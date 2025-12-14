package jp.kozu_osaka.android.kozuzen;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

import jp.kozu_osaka.android.kozuzen.exception.GetAccessException;
import jp.kozu_osaka.android.kozuzen.internal.InternalRegisteredAccountManager;
import jp.kozu_osaka.android.kozuzen.net.DataBaseAccessor;
import jp.kozu_osaka.android.kozuzen.net.DataBaseGetResponse;
import jp.kozu_osaka.android.kozuzen.net.argument.get.GetLatestVersionCodeArguments;
import jp.kozu_osaka.android.kozuzen.net.callback.GetAccessCallBack;
import jp.kozu_osaka.android.kozuzen.net.request.Request;
import jp.kozu_osaka.android.kozuzen.net.request.get.GetLatestVersionCodeRequest;
import jp.kozu_osaka.android.kozuzen.security.Secrets;
import jp.kozu_osaka.android.kozuzen.tutorial.TutorialListActivity;
import jp.kozu_osaka.android.kozuzen.util.DialogProvider;
import jp.kozu_osaka.android.kozuzen.util.NotificationProvider;

public final class AccountActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_account);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.layout_account_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView mailView = findViewById(R.id.view_account_mailAddress);
        Button checkPolicyButton = findViewById(R.id.view_account_viewPolicy);
        Button changePasswordButton = findViewById(R.id.view_account_changePassword);
        Button logoutButton = findViewById(R.id.view_account_logout);
        Button tutorialButton = findViewById(R.id.view_account_tutorial);
        Button updateButton = findViewById(R.id.view_account_checkUpdate);
        TextView versionLabel = findViewById(R.id.view_account_version);
        
        mailView.setText(InternalRegisteredAccountManager.getMailAddress());
        mailView.setOnClickListener(new OnMailViewClicked());
        versionLabel.setText(String.format(Locale.JAPAN, "Version: %s", BuildConfig.VERSION_NAME));
        checkPolicyButton.setOnClickListener(new OnCheckPolicyButtonClicked());
        tutorialButton.setOnClickListener(new OnCheckTutorialButtonClicked());
        changePasswordButton.setOnClickListener(new OnChangePasswordButtonClicked());
        updateButton.setOnClickListener(new OnUpdateButtonClicked());
        
        logoutButton.setOnClickListener(new OnLogoutButtonClicked());
    }

    private final class OnMailViewClicked implements TextView.OnClickListener {

        @Override
        public void onClick(View v) {
            TextView mailView = findViewById(R.id.view_account_mailAddress);
            ClipboardManager manager = (ClipboardManager)AccountActivity.this.getSystemService(Context.CLIPBOARD_SERVICE);
            manager.setPrimaryClip(ClipData.newPlainText("", mailView.getText().toString()));
            Toast toast = Toast.makeText(AccountActivity.this, AccountActivity.this.getString(R.string.text_account_toast_copySuccess), Toast.LENGTH_SHORT);
            toast.show();
        }
    }
    
    private final class OnCheckPolicyButtonClicked implements Button.OnClickListener {

        @Override
        public void onClick(View v) {
            Uri uri = Uri.parse(Secrets.PRIVACY_POLICY_URL);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }
    }

    private final class OnCheckTutorialButtonClicked implements Button.OnClickListener {

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(AccountActivity.this, TutorialListActivity.class);
            startActivity(intent);
        }
    }

    private final class OnChangePasswordButtonClicked implements Button.OnClickListener {

        @Override
        public void onClick(View v) {
            Intent resetPassIntent = new Intent(AccountActivity.this, ResetPasswordActivity.class);
            startActivity(resetPassIntent);
        }
    }

    private final class OnLogoutButtonClicked implements Button.OnClickListener {

        @Override
        public void onClick(View v) {
            AlertDialog.Builder builder = DialogProvider.makeBuilder(AccountActivity.this, R.string.text_account_dialog_title, R.string.text_account_dialog_msg);
            builder.setPositiveButton(R.string.text_account_dialog_yes, (dialog, which) -> {
                InternalRegisteredAccountManager.remove(AccountActivity.this);
                Intent loginIntent = new Intent(AccountActivity.this, LoginActivity.class);
                loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(loginIntent);
            });
            builder.setNegativeButton(R.string.text_account_dialog_no, (dialog, which) -> {
                dialog.dismiss();
            });
            builder.create().show();
        }
    }
    
    private final class OnUpdateButtonClicked implements Button.OnClickListener {

        @Override
        public void onClick(View v) {
            Toast.makeText(AccountActivity.this, R.string.toast_login_checkingUpDate, Toast.LENGTH_LONG).show();

            GetLatestVersionCodeRequest request = new GetLatestVersionCodeRequest(new GetLatestVersionCodeArguments());
            GetAccessCallBack<Integer> callBack = new CheckUpdateAccessCallback(request);
            DataBaseAccessor.sendGetRequest(request, callBack);
        }
    }

    private final class CheckUpdateAccessCallback extends GetAccessCallBack<Integer> {

        public CheckUpdateAccessCallback(GetLatestVersionCodeRequest request) {
            super(request);
        }

        @Override
        public void onSuccess(@NotNull DataBaseGetResponse response) {
            Integer responseResult = getRequest.resultParse(response.getResultJsonElement());
            if(BuildConfig.VERSION_CODE >= responseResult) {
                Toast.makeText(AccountActivity.this, R.string.toast_login_appIsLatest, Toast.LENGTH_LONG).show();
            } else if(BuildConfig.VERSION_CODE < responseResult) {
                if(KozuZen.getCurrentActivity() == null) {
                    NotificationProvider.sendNotification(
                            getString(R.string.notification_title_update_needUpdate),
                            NotificationProvider.NotificationIcon.DECHAIN_APP_ICON,
                            getString(R.string.notification_message_update_needUpdate)
                    );
                }
                Intent updateIntent = new Intent(AccountActivity.this, UpDateActivity.class);
                startActivity(updateIntent);
            }
        }

        @Override
        public void onFailure(@Nullable DataBaseGetResponse response) {
            @StringRes Integer msgID = null;
            if(response != null) {
                switch(response.getResponseCode()) {
                    case Request.RESPONSE_CODE_ARGUMENT_NULL:
                        msgID = R.string.error_argNull;
                        break;
                    case Request.RESPONSE_CODE_ARGUMENT_NON_SIGNATURES:
                        msgID = R.string.error_notFoundSignatures;
                        break;
                }
            }
            if(msgID == null) {
                Toast.makeText(AccountActivity.this, R.string.error_failed, Toast.LENGTH_LONG).show();
            } else {
                KozuZen.createErrorReport(new GetAccessException(msgID));
            }
        }

        @Override
        public void onTimeOut() {
            Toast.makeText(AccountActivity.this, R.string.error_failed, Toast.LENGTH_LONG).show();
        }
    }
}