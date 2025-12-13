package jp.kozu_osaka.android.kozuzen;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import jp.kozu_osaka.android.kozuzen.annotation.RequireIntentExtra;
import jp.kozu_osaka.android.kozuzen.exception.PostAccessException;
import jp.kozu_osaka.android.kozuzen.internal.InternalRegisteredAccountManager;
import jp.kozu_osaka.android.kozuzen.net.DataBaseAccessor;
import jp.kozu_osaka.android.kozuzen.net.DataBasePostResponse;
import jp.kozu_osaka.android.kozuzen.net.argument.post.ConfirmResetPassAuthArguments;
import jp.kozu_osaka.android.kozuzen.net.argument.post.RecreateResetPassAuthCodeArguments;
import jp.kozu_osaka.android.kozuzen.net.callback.PostAccessCallBack;
import jp.kozu_osaka.android.kozuzen.net.request.Request;
import jp.kozu_osaka.android.kozuzen.net.request.post.ConfirmResetPassAuthRequest;
import jp.kozu_osaka.android.kozuzen.net.request.post.RecreateResetPassAuthCodeRequest;
import jp.kozu_osaka.android.kozuzen.security.HashedString;

@RequireIntentExtra(extraKey = Constants.IntentExtraKey.ACCOUNT_CHANGED_PASSWORD, extraClazz = String.class)
public final class ResetPassAuthActivity extends AuthorizationActivityAbstract {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onAuthButtonClicked(View v) {
        DataBaseAccessor.showLoadFragment(this, R.id.frame_authorization_fragmentFrame);

        if(this.mailAddress == null) {
            KozuZen.createErrorReport(new IllegalArgumentException("mailAddress for authActivity is null."));
            return;
        }
        String enteredCode = getEnteredCode();
        if(enteredCode == null) {
            DataBaseAccessor.removeLoadFragment(this);
            return;
        }
        HashedString newPass = HashedString.as(getIntent().getStringExtra(Constants.IntentExtraKey.ACCOUNT_CHANGED_PASSWORD));
        if(newPass == null) {
            KozuZen.createErrorReport(new IllegalArgumentException("newPass for authActivity is null."));
            return;
        }


        ConfirmResetPassAuthRequest request = new ConfirmResetPassAuthRequest(new ConfirmResetPassAuthArguments(mailAddress, newPass, enteredCode));
        PostAccessCallBack callBack = new PostAccessCallBack(request) {
            //認証が成功
            @Override
            public void onSuccess(@NotNull DataBasePostResponse response) {
                //internalにパスワードの変更を保存
                HashedString newPass = HashedString.as(ResetPassAuthActivity.this.getIntent().getStringExtra(Constants.IntentExtraKey.ACCOUNT_CHANGED_PASSWORD));
                InternalRegisteredAccountManager.changePassword(Objects.requireNonNull(newPass));
                Toast.makeText(ResetPassAuthActivity.this, R.string.toast_resetPassAuth_success, Toast.LENGTH_LONG).show();
                Intent intent = new Intent(ResetPassAuthActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }

            //認証が失敗
            @Override
            public void onFailure(@Nullable DataBasePostResponse response) {
                if(response != null) {
                    switch(response.getResponseCode()) {
                        case Request.RESPONSE_CODE_ARGUMENT_NULL:
                            KozuZen.createErrorReport(new PostAccessException(R.string.error_argNull));
                            finish();
                            break;
                        case Request.RESPONSE_CODE_ARGUMENT_NON_SIGNATURES:
                            KozuZen.createErrorReport(new PostAccessException(R.string.error_notFoundSignatures));
                            finish();
                            break;
                        case ConfirmResetPassAuthRequest.ERROR_CODE_INCORRECT:
                            Toast.makeText(ResetPassAuthActivity.this, R.string.error_errorResponse_resetPassAuth_incorrect, Toast.LENGTH_LONG).show();
                            break;
                        case Request.RESPONSE_CODE_NOT_FOUND_REGED:
                            Toast.makeText(ResetPassAuthActivity.this, R.string.error_user_login_notFound_reged, Toast.LENGTH_LONG).show();
                            break;
                        case ConfirmResetPassAuthRequest.ERROR_CODE_NOT_FOUND_PASSLINE_OR_CODELINE:
                            KozuZen.createErrorReport(new PostAccessException(R.string.error_errorResponse_resetPassAuth_notFoundPassOrCodeLine));
                            finish();
                            return;
                    }
                } else {
                    Toast.makeText(ResetPassAuthActivity.this, R.string.error_failed, Toast.LENGTH_LONG).show();
                }
                DataBaseAccessor.removeLoadFragment(ResetPassAuthActivity.this);
            }

            @Override
            public void onTimeOut() {
                Toast.makeText(ResetPassAuthActivity.this, R.string.error_failed, Toast.LENGTH_LONG).show();
                DataBaseAccessor.removeLoadFragment(ResetPassAuthActivity.this);
            }
        };
        DataBaseAccessor.sendPostRequest(request, callBack);
    }

    @Override
    protected void onReAuthButtonClicked(View v) {
        DataBaseAccessor.showLoadFragment(this, R.id.frame_authorization_fragmentFrame);

        RecreateResetPassAuthCodeRequest request = new RecreateResetPassAuthCodeRequest(
                new RecreateResetPassAuthCodeArguments(mailAddress)
        );
        PostAccessCallBack callBack = new PostAccessCallBack(request) {
            @Override
            public void onSuccess(@NotNull DataBasePostResponse response) {
                Toast.makeText(ResetPassAuthActivity.this, R.string.toast_recreateCode_success, Toast.LENGTH_LONG).show();
                Intent authIntent = new Intent(ResetPassAuthActivity.this, ResetPassAuthActivity.class);
                authIntent.putExtra(Constants.IntentExtraKey.ACCOUNT_MAIL, mailAddress);
                authIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                ResetPassAuthActivity.this.startActivity(authIntent);
                DataBaseAccessor.removeLoadFragment(ResetPassAuthActivity.this);
            }

            @Override
            public void onFailure(@Nullable DataBasePostResponse response) {
                if(response != null) {
                    switch(response.getResponseCode()) {
                        case Request.RESPONSE_CODE_ARGUMENT_NULL:
                            KozuZen.createErrorReport(new PostAccessException(R.string.error_argNull));
                            finish();
                            break;
                        case Request.RESPONSE_CODE_ARGUMENT_NON_SIGNATURES:
                            KozuZen.createErrorReport(new PostAccessException(R.string.error_notFoundSignatures));
                            finish();
                            break;
                        case RecreateResetPassAuthCodeRequest.ERROR_CODE_NOT_FOUND_REQTIME_OR_REQCODE_LINE:
                            KozuZen.createErrorReport(new PostAccessException(R.string.error_errorResponse_recreateResetPassAuth_notFoundReqTimeOrCode));
                            finish();
                            break;
                    }
                }
                Toast.makeText(ResetPassAuthActivity.this, R.string.error_failed, Toast.LENGTH_LONG).show();
                DataBaseAccessor.removeLoadFragment(ResetPassAuthActivity.this);
            }

            @Override
            public void onTimeOut() {
                DataBaseAccessor.removeLoadFragment(ResetPassAuthActivity.this);
            }
        };
        DataBaseAccessor.sendPostRequest(request, callBack);
    }
}