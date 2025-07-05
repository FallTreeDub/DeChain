package jp.kozu_osaka.android.kozuzen;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.security.NoSuchAlgorithmException;

import jp.kozu_osaka.android.kozuzen.access.AccessThread;
import jp.kozu_osaka.android.kozuzen.access.task.foreground.ResetPasswordTask;
import jp.kozu_osaka.android.kozuzen.security.HashedString;
import jp.kozu_osaka.android.kozuzen.security.MailAddressChecker;
import jp.kozu_osaka.android.kozuzen.security.PasswordChecker;
import jp.kozu_osaka.android.kozuzen.util.ZenTextWatcher;

/**
 * パスワードのリセット画面。
 */
public final class ResetPasswordActivity extends AppCompatActivity {

    private final View.OnClickListener ENTER_BUTTON_ONCLICK = b -> {
        EditText mailAddressView = findViewById(R.id.editText_resetPass_mailAddress);
        EditText passwordView = findViewById(R.id.editText_resetPass_newPassword);

        String enteredMailAddress = mailAddressView.getText().toString();
        HashedString enteredPassword = null;
        try {
            enteredPassword = HashedString.encrypt(passwordView.getText().toString());
        } catch (NoSuchAlgorithmException e) {
            KozuZen.createErrorReport(this, e);
        }
        AccessThread thread = new AccessThread(
                new ResetPasswordTask(
                        ResetPasswordActivity.this, R.id.frame_resetPass_fragmentFrame,
                        enteredMailAddress, enteredPassword)
        );
        thread.start();
    };

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
        enterButton.setOnClickListener(this.ENTER_BUTTON_ONCLICK);
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
}