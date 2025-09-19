package jp.kozu_osaka.android.kozuzen;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.common.base.Preconditions;

import jp.kozu_osaka.android.kozuzen.access.DataBaseAccessor;
import jp.kozu_osaka.android.kozuzen.access.DataBasePostResponse;
import jp.kozu_osaka.android.kozuzen.access.argument.post.ConfirmAuthArguments;
import jp.kozu_osaka.android.kozuzen.access.argument.post.RecreateResetPassAuthCodeArguments;
import jp.kozu_osaka.android.kozuzen.access.argument.post.RecreateTentativeAuthCodeArguments;
import jp.kozu_osaka.android.kozuzen.access.callback.PostAccessCallBack;
import jp.kozu_osaka.android.kozuzen.access.request.post.ConfirmAuthRequest;
import jp.kozu_osaka.android.kozuzen.access.request.post.RecreateResetPassAuthCodeRequest;
import jp.kozu_osaka.android.kozuzen.access.request.post.RecreateTentativeAuthCodeRequest;
import jp.kozu_osaka.android.kozuzen.annotation.RequireIntentExtra;
import jp.kozu_osaka.android.kozuzen.security.SixNumberCode;
import jp.kozu_osaka.android.kozuzen.util.ZenTextWatcher;

/**
 * 6桁認証コードの確認画面。
 */
@RequireIntentExtra(extraClazz = String.class, extraKey = Constants.IntentExtraKey.ACCOUNT_MAIL)
@RequireIntentExtra(extraClazz = SixNumberCode.CodeType.class, extraKey = Constants.IntentExtraKey.SIX_AUTHORIZATION_CODE_TYPE)
public final class AuthorizationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_authorization);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //extraからの値
        String mailAddress = Preconditions.checkNotNull(getIntent().getStringExtra(Constants.IntentExtraKey.ACCOUNT_MAIL));
        SixNumberCode.CodeType enteredCodeType = (SixNumberCode.CodeType)Preconditions.checkNotNull(getIntent().getSerializableExtra(Constants.IntentExtraKey.SIX_AUTHORIZATION_CODE_TYPE));

        //認証コード入力欄設定
        LinearLayout editTextParent = findViewById(R.id.linear_authorization_editTextParent);
        for(int i = 0; i < editTextParent.getChildCount(); i++) {
            if(!(editTextParent.getChildAt(i) instanceof EditText)) continue;
            EditText focusEditText = (EditText)editTextParent.getChildAt(i);
            focusEditText.addTextChangedListener(new ZenTextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    focusEditText.clearFocus();
                    int forwardID = focusEditText.getNextFocusForwardId();
                    if(forwardID != View.NO_ID) findViewById(forwardID).requestFocus();
                }
            });
        }

        //ボタン設定
        Button authButton = findViewById(R.id.button_authorization_enter);
        Button reAuthButton = findViewById(R.id.button_authorization_enter_reSend);
        authButton.setOnClickListener(new OnAuthorizationButtonClicked(mailAddress, enteredCodeType));
        reAuthButton.setOnClickListener(new OnReAuthorizationButtonClicked(mailAddress, enteredCodeType));
    }

    private final class OnReAuthorizationButtonClicked implements Button.OnClickListener {

        private final String mailAddress;
        private final SixNumberCode.CodeType enteredCodeType;

        public OnReAuthorizationButtonClicked(String mailAddress, SixNumberCode.CodeType enteredCodeType) {
            this.mailAddress = mailAddress;
            this.enteredCodeType = enteredCodeType;
        }

        @Override
        public void onClick(View v) {
            PostAccessCallBack callBack = new PostAccessCallBack() {
                @Override
                public void onSuccess() {
                    Toast.makeText(AuthorizationActivity.this, KozuZen.getInstance().getString(R.string.toast_recreateCode_success), Toast.LENGTH_LONG).show();
                    Intent authIntent = new Intent(AuthorizationActivity.this, AuthorizationActivity.class);
                    authIntent.putExtra(Constants.IntentExtraKey.ACCOUNT_MAIL, mailAddress);
                    authIntent.putExtra(Constants.IntentExtraKey.SIX_AUTHORIZATION_CODE_TYPE, enteredCodeType);
                    authIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    AuthorizationActivity.this.startActivity(authIntent);
                }

                @Override
                public void onFailure(@Nullable DataBasePostResponse response) {
                    Toast.makeText(AuthorizationActivity.this, R.string.toast_timeout, Toast.LENGTH_LONG).show();
                    Intent authIntent = new Intent(AuthorizationActivity.this, AuthorizationActivity.class);
                    authIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    authIntent.putExtra(Constants.IntentExtraKey.ACCOUNT_MAIL, mailAddress);
                    authIntent.putExtra(Constants.IntentExtraKey.SIX_AUTHORIZATION_CODE_TYPE, enteredCodeType);
                    AuthorizationActivity.this.startActivity(authIntent);
                }

                @Override
                public void onTimeOut(DataBasePostResponse response) {
                    Toast.makeText(AuthorizationActivity.this, R.string.toast_timeout, Toast.LENGTH_LONG).show();
                    Intent authIntent = new Intent(AuthorizationActivity.this, AuthorizationActivity.class);
                    authIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    authIntent.putExtra(Constants.IntentExtraKey.ACCOUNT_MAIL, mailAddress);
                    authIntent.putExtra(Constants.IntentExtraKey.SIX_AUTHORIZATION_CODE_TYPE, enteredCodeType);
                    AuthorizationActivity.this.startActivity(authIntent);
                }
            };

            switch(enteredCodeType) {
                case FOR_CREATE_ACCOUNT:
                    RecreateTentativeAuthCodeRequest resetCreateAccountAuthReq = new RecreateTentativeAuthCodeRequest(
                            new RecreateTentativeAuthCodeArguments(mailAddress)
                    );
                    DataBaseAccessor.sendPostRequest(resetCreateAccountAuthReq, callBack);
                    break;
                case FOR_PASSWORD_RESET:
                    RecreateResetPassAuthCodeRequest resetPassAuthReq = new RecreateResetPassAuthCodeRequest(
                            new RecreateResetPassAuthCodeArguments(mailAddress)
                    );
                    DataBaseAccessor.sendPostRequest(resetPassAuthReq, callBack);
                    break;
            }
        }
    }

    private final class OnAuthorizationButtonClicked implements Button.OnClickListener {

        private final String mailAddress;
        private final SixNumberCode.CodeType enteredCodeType;

        public OnAuthorizationButtonClicked(String mailAddress, SixNumberCode.CodeType enteredCodeType) {
            this.mailAddress = mailAddress;
            this.enteredCodeType = enteredCodeType;
        }

        @Override
        public void onClick(View v) {
            String enteredCode = getEnteredCode();
            if(enteredCode == null) {
                return;
            }

            try {
                PostAccessCallBack callBack = new PostAccessCallBack() {

                    //認証が成功
                    @Override
                    public void onSuccess() {
                        if(enteredCodeType.equals(SixNumberCode.CodeType.FOR_CREATE_ACCOUNT)) {
                            Intent intent = new Intent(AuthorizationActivity.this, HomeActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        } else if(enteredCodeType.equals(SixNumberCode.CodeType.FOR_PASSWORD_RESET)) {
                            Toast.makeText(AuthorizationActivity.this, R.string.toast_resetPassAuth_success, Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(AuthorizationActivity.this, LoginActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }
                    }

                    //認証が失敗
                    @Override
                    public void onFailure(@Nullable DataBasePostResponse response) {
                        if(enteredCodeType.equals(SixNumberCode.CodeType.FOR_CREATE_ACCOUNT)) {
                            Intent intent = new Intent(AuthorizationActivity.this, AuthorizationActivity.class);
                            intent.putExtra(Constants.IntentExtraKey.ACCOUNT_MAIL, mailAddress);
                            intent.putExtra(Constants.IntentExtraKey.SIX_AUTHORIZATION_CODE_TYPE, enteredCodeType);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        } else if(enteredCodeType.equals(SixNumberCode.CodeType.FOR_PASSWORD_RESET)) {
                            Toast.makeText(AuthorizationActivity.this, R.string.toast_resetPassAuth_failure, Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(AuthorizationActivity.this, ResetPasswordActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }
                    }

                    @Override
                    public void onTimeOut(DataBasePostResponse response) {
                        Toast.makeText(AuthorizationActivity.this, R.string.toast_timeout, Toast.LENGTH_LONG).show();
                        Intent loginIntent = new Intent(AuthorizationActivity.this, LoginActivity.class);
                        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(loginIntent);
                    }
                };
                DataBaseAccessor.sendPostRequest(
                        new ConfirmAuthRequest(enteredCodeType, new ConfirmAuthArguments(mailAddress, enteredCode)),
                        callBack
                );
            } catch(Exception e) {
                KozuZen.createErrorReport(AuthorizationActivity.this, e);
            }
        }

        /**
         * @return 入力された6桁コード。無効な書式の場合は{@code null}が返される。
         */
        private String getEnteredCode() {
            //コード入力確認
            LinearLayout editTextParent = findViewById(R.id.linear_authorization_editTextParent);
            StringBuilder builder = new StringBuilder();
            for(int i = 0; i < editTextParent.getChildCount(); i++) {
                if(!(editTextParent.getChildAt(i) instanceof EditText)) {
                    continue;
                }
                EditText editText = (EditText)editTextParent.getChildAt(i);
                String text = editText.getText().toString();
                if(!text.isEmpty()) {
                    builder.append(text);
                } else {
                    editText.setError(getString(R.string.text_authorization_warn));
                    return null;
                }
            }
            return builder.toString();
        }
    }
}
