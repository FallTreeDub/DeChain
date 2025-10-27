package jp.kozu_osaka.android.kozuzen;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.security.NoSuchAlgorithmException;

import jp.kozu_osaka.android.kozuzen.access.DataBaseAccessor;
import jp.kozu_osaka.android.kozuzen.access.DataBasePostResponse;
import jp.kozu_osaka.android.kozuzen.access.argument.post.ResetPasswordArguments;
import jp.kozu_osaka.android.kozuzen.access.callback.PostAccessCallBack;
import jp.kozu_osaka.android.kozuzen.access.request.post.ResetPasswordRequest;
import jp.kozu_osaka.android.kozuzen.security.HashedString;
import jp.kozu_osaka.android.kozuzen.security.MailAddressChecker;
import jp.kozu_osaka.android.kozuzen.security.PasswordChecker;
import jp.kozu_osaka.android.kozuzen.security.SixNumberCode;
import jp.kozu_osaka.android.kozuzen.util.ZenTextWatcher;

/**
 * パスワードのリセット画面。
 */
public final class ResetPasswordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reset_password);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button enterButton = findViewById(R.id.button_resetPass_enter);
        enterButton.setOnClickListener(new OnEnterButtonClicked());
        enterButton.setEnabled(false);

        EditText mailAddressView = findViewById(R.id.editText_resetPass_mailAddress);
        mailAddressView.addTextChangedListener(new ZenTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean isValid = MailAddressChecker.checkMailAddress(mailAddressView.getText().toString());
                mailAddressView.setError(isValid ? null : getString(R.string.text_resetPass_warn_mailAddress));
                enterButton.setEnabled(isValid);
            }
        });

        EditText newPassView = findViewById(R.id.editText_resetPass_newPassword);
        newPassView.addTextChangedListener(new ZenTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                PasswordChecker.SafetyStatus status = PasswordChecker.checkPassword(newPassView.getText().toString());
                String msg = "";
                if(!status.isOnlyAlnumsAndSymbols()) {
                    msg += getString(R.string.text_resetPass_passwordCondition_warn1) + "\n";
                }
                if(!status.isRangeInLimit()) {
                    msg += getString(R.string.text_resetPass_passwordCondition_warn2) + "\n";
                }
                if(!status.meetsMinLenOfAlnumsAndSymbols()) {
                    msg += getString(R.string.text_resetPass_passwordCondition_warn3) + "\n";
                }
                newPassView.setError(msg.isEmpty() ? null : msg);
                enterButton.setEnabled(msg.isEmpty());
            }
        });

        EditText newPassCheckView = findViewById(R.id.editText_resetPass_newPasswordCheck);
        newPassCheckView.addTextChangedListener(new ZenTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                newPassCheckView.setError(
                        newPassView.getText().toString().equals(newPassCheckView.getText().toString())
                        ? null : getString(R.string.text_resetPass_warn_passwordMatch)
                );

            }
        });
    }

    private class OnEnterButtonClicked implements Button.OnClickListener {

        @Override
        public void onClick(View v) {
            EditText mailAddressView = findViewById(R.id.editText_resetPass_mailAddress);
            EditText passwordView = findViewById(R.id.editText_resetPass_newPassword);

            String enteredMailAddress = mailAddressView.getText().toString();
            HashedString enteredPassword;
            try {
                enteredPassword = HashedString.encrypt(passwordView.getText().toString());
                ResetPasswordRequest request = new ResetPasswordRequest(new ResetPasswordArguments(enteredMailAddress, enteredPassword));

                PostAccessCallBack callBack = new PostAccessCallBack(request) {
                    @Override
                    public void onSuccess(DataBasePostResponse response) {
                        Intent intent = new Intent(ResetPasswordActivity.this, AuthorizationActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.putExtra(Constants.IntentExtraKey.ACCOUNT_MAIL, enteredMailAddress);
                        intent.putExtra(Constants.IntentExtraKey.SIX_AUTHORIZATION_CODE_TYPE, SixNumberCode.CodeType.FOR_PASSWORD_RESET);
                        ResetPasswordActivity.this.startActivity(intent);
                    }

                    @Override
                    public void onFailure(@Nullable DataBasePostResponse response) {
                        Toast.makeText(ResetPasswordActivity.this, KozuZen.getInstance().getString(R.string.toast_resetPass_failure), Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        ResetPasswordActivity.this.startActivity(intent);
                    }

                    @Override
                    public void onTimeOut(DataBasePostResponse response) {
                        //
                    }
                };
                DataBaseAccessor.sendPostRequest(request, callBack);
            } catch (NoSuchAlgorithmException e) {
                KozuZen.createErrorReport(ResetPasswordActivity.this, e);
            }
        }
    }
}