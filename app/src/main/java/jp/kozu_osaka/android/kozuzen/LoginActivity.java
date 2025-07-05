package jp.kozu_osaka.android.kozuzen;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.security.NoSuchAlgorithmException;

import jp.kozu_osaka.android.kozuzen.access.AccessThread;
import jp.kozu_osaka.android.kozuzen.access.task.foreground.InquiryTask;
import jp.kozu_osaka.android.kozuzen.security.HashedString;
import jp.kozu_osaka.android.kozuzen.security.MailAddressChecker;
import jp.kozu_osaka.android.kozuzen.util.ZenTextWatcher;

/**
 * ログイン画面を担当するActivity。
 */
public final class LoginActivity extends AppCompatActivity {

    private boolean isValidMail = false;
    private boolean isValidPass = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        EditText passwordView = findViewById(R.id.password);
        EditText mailAddressView = findViewById(R.id.mailAddress);
        Button loginButton = findViewById(R.id.login_button);
        Button resetPassButton = findViewById(R.id.button_login_resetPass);
        Button createAccountButton = findViewById(R.id.createAccount_button);

        mailAddressView.addTextChangedListener(new ZenTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean isValid = MailAddressChecker.checkMailAddress(mailAddressView.getText().toString());
                mailAddressView.setError(isValid ? null : getString(R.string.text_login_mailWarn));
                setIsValidMail(isValid);
            }
        });
        passwordView.addTextChangedListener(new ZenTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean isValid = !(passwordView.getText().toString().isEmpty() || passwordView.getText() == null);
                passwordView.setError(isValid ? null : getString(R.string.text_login_passWarn));
                setIsValidPass(isValid);
            }
        });

        loginButton.setOnClickListener(b -> {
            String enteredMail = mailAddressView.getText().toString();
            HashedString enteredPass = null;
            try {
                enteredPass = HashedString.encrypt(passwordView.getText().toString());
            } catch (NoSuchAlgorithmException e) {
                KozuZen.createErrorReport(this, e);
            }

            AccessThread accessThread = new AccessThread(
                    new InquiryTask(
                            this,
                            R.id.frame_login_fragmentFrame,
                            enteredMail,
                            enteredPass)
            );
            accessThread.start();
        });

        resetPassButton.setOnClickListener(b -> {
            Intent intent = new Intent(this, ResetPasswordActivity.class);
            startActivity(intent);
        });

        createAccountButton.setOnClickListener(b -> {
            Intent intent = new Intent(this, CreateAccountActivity.class);
            startActivity(intent);
        });
    }

    private void setIsValidMail(boolean flag) {
        this.isValidMail = flag;
        switchEnableLoginButton();
    }

    private void setIsValidPass(boolean flag) {
        this.isValidPass = flag;
        switchEnableLoginButton();
    }

    private void switchEnableLoginButton() {
        Button loginButton = findViewById(R.id.login_button);
        loginButton.setEnabled(this.isValidMail && this.isValidPass);
    }
}