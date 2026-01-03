package jp.kozu_osaka.android.kozuzen;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import jp.kozu_osaka.android.kozuzen.exception.PostAccessException;
import jp.kozu_osaka.android.kozuzen.internal.InternalRegisteredAccountManager;
import jp.kozu_osaka.android.kozuzen.internal.InternalTentativeAccountManager;
import jp.kozu_osaka.android.kozuzen.net.DataBaseAccessor;
import jp.kozu_osaka.android.kozuzen.net.DataBasePostResponse;
import jp.kozu_osaka.android.kozuzen.net.argument.post.ConfirmTentativeAuthArguments;
import jp.kozu_osaka.android.kozuzen.net.argument.post.RecreateTentativeAuthCodeArguments;
import jp.kozu_osaka.android.kozuzen.net.callback.PostAccessCallBack;
import jp.kozu_osaka.android.kozuzen.net.request.Request;
import jp.kozu_osaka.android.kozuzen.net.request.post.ConfirmTentativeAuthRequest;
import jp.kozu_osaka.android.kozuzen.net.request.post.RecreateTentativeAuthCodeRequest;

/**
 * 仮登録の登録時の6桁認証のコード入力画面。
 */
public final class CreateAccountAuthActivity extends AuthorizationActivityAbstract {

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
            return;
        }

        ConfirmTentativeAuthRequest request = new ConfirmTentativeAuthRequest(new ConfirmTentativeAuthArguments(mailAddress, enteredCode));
        PostAccessCallBack callBack = new PostAccessCallBack(request) {
            //認証が成功
            @Override
            public void onSuccess(@NotNull DataBasePostResponse response) {
                //internalに保存
                if(response.getResponseMessage() == null) {
                    KozuZen.createErrorReport(
                            new PostAccessException(R.string.error_database_auth_experimentTypeIsNull)
                    );
                    DataBaseAccessor.removeLoadFragment(CreateAccountAuthActivity.this);
                    return;
                }
                ExperimentType type = ExperimentType.getFromID(Integer.parseInt(response.getResponseMessage()));
                if(type == null) {
                    KozuZen.createErrorReport(
                            new PostAccessException(R.string.error_database_auth_experimentTypeIsInvalid)
                    );
                    DataBaseAccessor.removeLoadFragment(CreateAccountAuthActivity.this);
                    return;
                }
                InternalRegisteredAccountManager.register(
                        CreateAccountAuthActivity.this,
                        InternalTentativeAccountManager.getMailAddress(),
                        InternalTentativeAccountManager.getEncryptedPassword(),
                        type);
                //ホーム画面に遷移
                Intent intent = new Intent(CreateAccountAuthActivity.this, HomeActivity.class);
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
                    }
                }
            }

            @Override
            public void onTimeOut() {
                Toast.makeText(CreateAccountAuthActivity.this, R.string.error_failed, Toast.LENGTH_LONG).show();
                DataBaseAccessor.removeLoadFragment(CreateAccountAuthActivity.this);
            }
        };
        DataBaseAccessor.sendPostRequest(request, callBack);
    }

    @Override
    protected void onReAuthButtonClicked(View v) {
        DataBaseAccessor.showLoadFragment(this, R.id.frame_authorization_fragmentFrame);

        RecreateTentativeAuthCodeRequest request = new RecreateTentativeAuthCodeRequest(
                new RecreateTentativeAuthCodeArguments(mailAddress)
        );
        PostAccessCallBack callBack = new PostAccessCallBack(request) {
            @Override
            public void onSuccess(@NotNull DataBasePostResponse response) {
                Toast.makeText(CreateAccountAuthActivity.this, R.string.toast_recreateCode_success, Toast.LENGTH_LONG).show();
                Intent authIntent = new Intent(CreateAccountAuthActivity.this, CreateAccountAuthActivity.class);
                authIntent.putExtra(Constants.IntentExtraKey.ACCOUNT_MAIL, mailAddress);
                authIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                CreateAccountAuthActivity.this.startActivity(authIntent);
                DataBaseAccessor.removeLoadFragment(CreateAccountAuthActivity.this);
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
                    }
                }
                Toast.makeText(CreateAccountAuthActivity.this, R.string.error_failed, Toast.LENGTH_LONG).show();
                DataBaseAccessor.removeLoadFragment(CreateAccountAuthActivity.this);
            }

            @Override
            public void onTimeOut() {
                DataBaseAccessor.removeLoadFragment(CreateAccountAuthActivity.this);
            }
        };
        DataBaseAccessor.sendPostRequest(request, callBack);
    }
}