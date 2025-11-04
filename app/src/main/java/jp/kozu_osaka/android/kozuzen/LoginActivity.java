package jp.kozu_osaka.android.kozuzen;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.jetbrains.annotations.NotNull;

import java.security.NoSuchAlgorithmException;
import java.security.Permission;

import jp.kozu_osaka.android.kozuzen.access.DataBaseAccessor;
import jp.kozu_osaka.android.kozuzen.access.argument.get.GetLatestVersionCodeArguments;
import jp.kozu_osaka.android.kozuzen.access.argument.get.GetRegisteredExistenceArguments;
import jp.kozu_osaka.android.kozuzen.access.callback.GetAccessCallBack;
import jp.kozu_osaka.android.kozuzen.access.request.get.GetLatestVersionCodeRequest;
import jp.kozu_osaka.android.kozuzen.access.request.get.GetRegisteredExistenceRequest;
import jp.kozu_osaka.android.kozuzen.exception.GetAccessException;
import jp.kozu_osaka.android.kozuzen.exception.NotAllowedPermissionException;
import jp.kozu_osaka.android.kozuzen.internal.InternalRegisteredAccountManager;
import jp.kozu_osaka.android.kozuzen.security.HashedString;
import jp.kozu_osaka.android.kozuzen.security.MailAddressChecker;
import jp.kozu_osaka.android.kozuzen.util.Logger;
import jp.kozu_osaka.android.kozuzen.util.PermissionsStatus;
import jp.kozu_osaka.android.kozuzen.util.ZenTextWatcher;
import okhttp3.Call;

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
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.layout_login_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        EditText passwordView = findViewById(R.id.editText_login_password);
        EditText mailAddressView = findViewById(R.id.editText_login_mailAddress);
        Button loginButton = findViewById(R.id.login_button);
        Button resetPassButton = findViewById(R.id.button_login_resetPass);
        Button createAccountButton = findViewById(R.id.createAccount_button);
        Button checkUpDateButton = findViewById(R.id.view_button_login_checkUpDate);
        ImageButton infoButton = findViewById(R.id.view_button_login_info);

        mailAddressView.addTextChangedListener(new MailAddressTextWatcher());
        passwordView.addTextChangedListener(new PasswordTextWatcher());
        loginButton.setOnClickListener(new OnLogInButtonClicked());
        resetPassButton.setOnClickListener(new OnPasswordResetButtonClicked());
        createAccountButton.setOnClickListener(new OnCreateAccountButtonClicked());
        infoButton.setOnClickListener(new OnInfoButtonClicked());
        checkUpDateButton.setOnClickListener(new OnCheckUpDateButtonClicked());

        if(!PermissionsStatus.isAllowedInstallPackage()) {
            PermissionsStatus.createDialogInstallPackages(LoginActivity.this, () -> {}, () -> {}).show();
        }
        if(!PermissionsStatus.isAllowedNotification()) {
            PermissionsStatus.createDialogNotification(LoginActivity.this, () -> {}, () -> {}).show();
        }
        if(!PermissionsStatus.isAllowedAppUsageStats()) {
            PermissionsStatus.createDialogAppUsageStats(LoginActivity.this, () -> {}, () -> {}).show();
        }
        if(!PermissionsStatus.isAllowedScheduleAlarm()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PermissionsStatus.createDialogExactAlarm(LoginActivity.this, () -> {}, () -> {}).show();
            }
        }
    }

    private void setIsValidMail(boolean flag) {
        this.isValidMail = flag;
        switchEnableLoginButton();
    }

    private void setIsValidPass(boolean flag) {
        this.isValidPass = flag;
        switchEnableLoginButton();
    }

    /**
     * メールアドレスとパスワードが両方有効ならばログインボタンを有効化する。
     */
    private void switchEnableLoginButton() {
        Button loginButton = findViewById(R.id.login_button);
        loginButton.setEnabled(this.isValidMail && this.isValidPass);
    }

    private final class MailAddressTextWatcher extends ZenTextWatcher {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            EditText mailAddressView = findViewById(R.id.editText_login_mailAddress);
            boolean isValid = MailAddressChecker.checkMailAddress(mailAddressView.getText().toString());
            mailAddressView.setError(isValid ? null : getString(R.string.text_login_mailWarn));
            setIsValidMail(isValid);
        }
    }

    private final class PasswordTextWatcher extends ZenTextWatcher {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            EditText passwordView = findViewById(R.id.editText_login_password);
            boolean isValid = !(passwordView.getText().toString().isEmpty() || passwordView.getText() == null);
            passwordView.setError(isValid ? null : getString(R.string.text_login_passWarn));
            setIsValidPass(isValid);
        }
    }

    /**
     * ログイン処理でデータベースとの通信の際にコールバックとして使うクラス。
     */
    private final class LoginCallBack extends GetAccessCallBack<Integer> {

        private final String mail;
        private final HashedString pass;

        public LoginCallBack(GetRegisteredExistenceRequest request, String mail, HashedString pass) {
            super(request);
            this.mail = mail;
            this.pass = pass;
        }

        @Override
        public void onSuccess(@NotNull Integer accountExperimentType) {
            Logger.i(accountExperimentType);
            ExperimentType type = ExperimentType.getFromID(accountExperimentType);
            if (type != null) {
                try {
                    InternalRegisteredAccountManager.register(LoginActivity.this, mail, pass, type);
                } catch(NotAllowedPermissionException e) {
                    KozuZen.createErrorReport(LoginActivity.this, e);
                }
                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                LoginActivity.this.startActivity(intent);
            } else {
                Toast.makeText(LoginActivity.this, R.string.toast_inquiry_notFound, Toast.LENGTH_LONG).show();
                if(InternalRegisteredAccountManager.isRegistered()) {
                    InternalRegisteredAccountManager.remove(LoginActivity.this);
                }
            }
            DataBaseAccessor.removeLoadFragment(LoginActivity.this);
        }

        @Override
        public void onFailure(int responseCode, String message) {
            Toast.makeText(LoginActivity.this, R.string.toast_inquiry_notFound, Toast.LENGTH_LONG).show();
            DataBaseAccessor.removeLoadFragment(LoginActivity.this);
        }

        @Override
        public void onTimeOut() {
            Toast.makeText(LoginActivity.this, LoginActivity.this.getString(R.string.toast_failure_timeout), Toast.LENGTH_LONG).show();
            DataBaseAccessor.removeLoadFragment(LoginActivity.this);
        }
    }

    /**
     * データベース上に本登録アカウントが存在することを確認し、{@link LoginCallBack}のコールバックを実行する。
     * @see LoginCallBack
     */
    private final class OnLogInButtonClicked implements Button.OnClickListener {

        @Override
        public void onClick(View v) {
            EditText passwordView = findViewById(R.id.editText_login_password);
            EditText mailAddressView = findViewById(R.id.editText_login_mailAddress);

            String enteredMail = mailAddressView.getText().toString();
            HashedString enteredPass;
            try {
                enteredPass = HashedString.encrypt(passwordView.getText().toString());
            } catch (NoSuchAlgorithmException e) {
                KozuZen.createErrorReport(LoginActivity.this, e);
                return;
            }
            DataBaseAccessor.showLoadFragment(LoginActivity.this, R.id.frame_login_fragmentFrame);
            GetRegisteredExistenceRequest request = new GetRegisteredExistenceRequest(new GetRegisteredExistenceArguments(enteredMail, enteredPass));
            DataBaseAccessor.sendGetRequest(request, new LoginCallBack(request, enteredMail, enteredPass));
        }
    }

    /**
     *
     */
    private final class OnPasswordResetButtonClicked implements Button.OnClickListener {

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(LoginActivity.this, ResetPasswordActivity.class);
            startActivity(intent);
        }
    }

    /**
     *
     */
    private final class OnCreateAccountButtonClicked implements Button.OnClickListener {

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(LoginActivity.this, CreateAccountActivity.class);
            startActivity(intent);
        }
    }

    private final class OnInfoButtonClicked implements Button.OnClickListener {

        @Override
        public void onClick(View v) {
            //todo
        }
    }

    private final class OnCheckUpDateButtonClicked implements Button.OnClickListener {

        @Override
        public void onClick(View v) {
            Toast.makeText(LoginActivity.this, R.string.toast_login_checkingUpDate, Toast.LENGTH_LONG).show();

            GetLatestVersionCodeRequest request = new GetLatestVersionCodeRequest(new GetLatestVersionCodeArguments());
            GetAccessCallBack<Integer> callBack = new GetAccessCallBack<>(request) {
                @Override
                public void onSuccess(@NotNull Integer responseResult) {
                    if(KozuZen.VERSION_CODE == responseResult) {
                        Toast.makeText(LoginActivity.this, R.string.toast_login_appIsLatest, Toast.LENGTH_LONG).show();
                    } else {
                        Intent updateIntent = new Intent(LoginActivity.this, UpDateActivity.class);
                        startActivity(updateIntent);
                    }
                }

                @Override
                public void onFailure(int responseCode, String message) {
                    KozuZen.createErrorReport(LoginActivity.this, new GetAccessException(responseCode, message));
                }

                @Override
                public void onTimeOut() {
                    Toast.makeText(LoginActivity.this, R.string.toast_login_error, Toast.LENGTH_LONG).show();
                }
            };
            DataBaseAccessor.sendGetRequest(request, callBack);
        }
    }
}