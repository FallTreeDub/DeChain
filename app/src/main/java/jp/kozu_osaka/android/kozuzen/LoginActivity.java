package jp.kozu_osaka.android.kozuzen;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.security.NoSuchAlgorithmException;

import jp.kozu_osaka.android.kozuzen.net.DataBaseAccessor;
import jp.kozu_osaka.android.kozuzen.net.argument.get.GetLatestVersionCodeArguments;
import jp.kozu_osaka.android.kozuzen.net.argument.get.GetRegisteredExistenceArguments;
import jp.kozu_osaka.android.kozuzen.net.argument.get.GetTentativeExistenceArguments;
import jp.kozu_osaka.android.kozuzen.net.callback.GetAccessCallBack;
import jp.kozu_osaka.android.kozuzen.net.request.get.GetLatestVersionCodeRequest;
import jp.kozu_osaka.android.kozuzen.net.request.get.GetRegisteredExistenceRequest;
import jp.kozu_osaka.android.kozuzen.net.request.get.GetRequest;
import jp.kozu_osaka.android.kozuzen.net.request.get.GetTentativeExistenceRequest;
import jp.kozu_osaka.android.kozuzen.exception.GetAccessException;
import jp.kozu_osaka.android.kozuzen.exception.NotAllowedPermissionException;
import jp.kozu_osaka.android.kozuzen.internal.InternalBackgroundErrorReportManager;
import jp.kozu_osaka.android.kozuzen.internal.InternalRegisteredAccountManager;
import jp.kozu_osaka.android.kozuzen.internal.InternalTentativeAccountManager;
import jp.kozu_osaka.android.kozuzen.security.HashedString;
import jp.kozu_osaka.android.kozuzen.security.MailAddressChecker;
import jp.kozu_osaka.android.kozuzen.net.update.DeChainUpDater;
import jp.kozu_osaka.android.kozuzen.util.Logger;
import jp.kozu_osaka.android.kozuzen.util.NotificationProvider;
import jp.kozu_osaka.android.kozuzen.util.PermissionsStatus;
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

        //権限確認
        if(!PermissionsStatus.isAllowedInstallPackage()) {
            PermissionsStatus.createDialogInstallPackages(LoginActivity.this, () -> {}, () -> {}).show();
        }
        if(!PermissionsStatus.isAllowedNotification()) {
            PermissionsStatus.createDialogNotification(LoginActivity.this).show();
        }
        if(!PermissionsStatus.isAllowedAppUsageStats()) {
            PermissionsStatus.createDialogAppUsageStats(LoginActivity.this).show();
        }
        if(!PermissionsStatus.isAllowedScheduleAlarm()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PermissionsStatus.createDialogExactAlarm(LoginActivity.this).show();
            }
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();

        //backgroundエラー確認
        String report = InternalBackgroundErrorReportManager.get();
        if(report != null) {
            Logger.i("background report found.");
            Intent reportIntent = new Intent(this, ReportActivity.class);
            reportIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            reportIntent.putExtra(Constants.IntentExtraKey.REPORT_BODY, report);
            startActivity(reportIntent);
        }

        //アップデート状況確認(インストール準備整っている場合はリクエストダイアログ表示)
        if(DeChainUpDater.getStatus(this) == DeChainUpDater.UpDaterStatus.STATUS_SUCCESS) {
            int sessionID = DeChainUpDater.getInstallingSessionID(this);
            String installedAPKPath = DeChainUpDater.getInstalledAPKPath(this);
            if(sessionID != -1 && installedAPKPath != null) {
                try {
                    DeChainUpDater.showUpdateRequestDialog(this, new File(installedAPKPath), sessionID);
                } catch(FileNotFoundException e) {
                    KozuZen.createErrorReport(this, e);
                } catch(SecurityException e) {
                    DeChainUpDater.removeInstallingInfo(this);
                }
            } else {
                KozuZen.createErrorReport(this, new IllegalArgumentException("sessionID or installedAPKPath is invalid. sessionID:" + sessionID + ", installedAPKPath: " + installedAPKPath));
            }
            DeChainUpDater.setStatus(this, DeChainUpDater.UpDaterStatus.STATUS_STOPPING);
        }

        //ログイン状況確認
        if(InternalRegisteredAccountManager.isRegistered()) {
            //ログイン済みとして登録されたアカウントがある場合
            Logger.i("internal registered account exists.");
            GetRegisteredExistenceRequest request = new GetRegisteredExistenceRequest(
                    new GetRegisteredExistenceArguments(InternalRegisteredAccountManager.getMailAddress(), InternalRegisteredAccountManager.getEncryptedPassword())
            );
            RegisteredAccountExistenceCallBack callBack = new RegisteredAccountExistenceCallBack(request);
            DataBaseAccessor.sendGetRequest(request, callBack);
        } else {
            if(InternalTentativeAccountManager.isRegistered()) {
                //仮登録内部アカウントが存在する場合
                Logger.i("internal tentative account exists.");
                GetTentativeExistenceRequest request = new GetTentativeExistenceRequest(
                        new GetTentativeExistenceArguments(InternalTentativeAccountManager.getMailAddress())
                );
                TentativeAccountExistenceCallBack callback = new TentativeAccountExistenceCallBack(request);
                DataBaseAccessor.sendGetRequest(request, callback);
            }
            Logger.i("internal tentative account does not exist.");
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

    private final class RegisteredAccountExistenceCallBack extends GetAccessCallBack<Integer> {

        public RegisteredAccountExistenceCallBack(GetRequest<Integer> getRequest) {
            super(getRequest);
        }

        @Override
        public void onSuccess(@NotNull Integer accountExperimentType) {
            ExperimentType type = ExperimentType.getFromID(accountExperimentType);
            if(type != null) {
                Logger.i("registered found.");
                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            } else {
                Logger.i("registered not found.");
                InternalRegisteredAccountManager.remove(LoginActivity.this);
                Toast.makeText(LoginActivity.this, R.string.toast_inquiry_notFound, Toast.LENGTH_LONG).show();
                Intent intent = new Intent(LoginActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        }

        @Override
        public void onFailure(int responseCode, String message) {
            Toast.makeText(LoginActivity.this, R.string.toast_failure_closeApp, Toast.LENGTH_LONG).show();
            InternalRegisteredAccountManager.remove(LoginActivity.this);
        }

        @Override
        public void onTimeOut() {
            retry();
            InternalRegisteredAccountManager.remove(LoginActivity.this);
            Toast.makeText(LoginActivity.this, R.string.toast_failure_wait, Toast.LENGTH_LONG).show();
            Intent loginIntent = new Intent(LoginActivity.this, LoginActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            LoginActivity.this.startActivity(loginIntent);
        }
    }

    private final class TentativeAccountExistenceCallBack extends GetAccessCallBack<Boolean> {

        public TentativeAccountExistenceCallBack(GetRequest<Boolean> getRequest) {
            super(getRequest);
        }

        @Override
        public void onSuccess(@NotNull Boolean existsAccount) {
            if(existsAccount) {
                Intent authIntent = new Intent(LoginActivity.this, AuthorizationActivity.class);
                authIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                authIntent.putExtra(Constants.IntentExtraKey.ACCOUNT_MAIL, InternalTentativeAccountManager.getMailAddress());
                authIntent.putExtra(Constants.IntentExtraKey.ACCOUNT_ENCRYPTED_PASSWORD, InternalTentativeAccountManager.getEncryptedPassword());
                LoginActivity.this.startActivity(authIntent);
            } else {
                InternalTentativeAccountManager.remove();
            }
        }

        @Override
        public void onFailure(int responseCode, String message) {
            Toast.makeText(LoginActivity.this, R.string.toast_failure_wait, Toast.LENGTH_LONG).show();
            InternalTentativeAccountManager.remove();
            Intent loginIntent = new Intent(LoginActivity.this, LoginActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            LoginActivity.this.startActivity(loginIntent);
        }

        @Override
        public void onTimeOut() {
            retry();
            InternalTentativeAccountManager.remove();
            Toast.makeText(LoginActivity.this, LoginActivity.this.getString(R.string.toast_failure_wait), Toast.LENGTH_LONG).show();
            Intent loginIntent = new Intent(LoginActivity.this, LoginActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            LoginActivity.this.startActivity(loginIntent);
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
                        if(KozuZen.getCurrentActivity() == null) {
                            NotificationProvider.sendNotification(
                                    getString(R.string.notification_title_update_needUpdate),
                                    NotificationProvider.NotificationIcon.DECHAIN_APP_ICON,
                                    getString(R.string.notification_message_update_needUpdate)
                            );
                        }
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
                    retry();
                    Toast.makeText(LoginActivity.this, R.string.toast_login_error, Toast.LENGTH_LONG).show();
                }
            };
            DataBaseAccessor.sendGetRequest(request, callBack);
        }
    }
}