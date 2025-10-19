package jp.kozu_osaka.android.kozuzen;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.jetbrains.annotations.NotNull;

import jp.kozu_osaka.android.kozuzen.access.DataBaseAccessor;
import jp.kozu_osaka.android.kozuzen.access.argument.get.GetRegisteredExistenceArguments;
import jp.kozu_osaka.android.kozuzen.access.argument.get.GetTentativeExistenceArguments;
import jp.kozu_osaka.android.kozuzen.access.callback.GetAccessCallBack;
import jp.kozu_osaka.android.kozuzen.access.request.get.GetRegisteredExistenceRequest;
import jp.kozu_osaka.android.kozuzen.access.request.get.GetRequest;
import jp.kozu_osaka.android.kozuzen.access.request.get.GetTentativeExistenceRequest;
import jp.kozu_osaka.android.kozuzen.internal.InternalBackgroundErrorReportManager;
import jp.kozu_osaka.android.kozuzen.internal.InternalRegisteredAccountManager;
import jp.kozu_osaka.android.kozuzen.internal.InternalTentativeAccountManager;
import jp.kozu_osaka.android.kozuzen.util.Logger;

/**
 * 内部アカウントの有無、バックグラウンド時のエラー処理を行う。
 */
public final class InitActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.frame_loading_fragmentFrame), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Fragment表示
        DataBaseAccessor.showLoadFragment(this, R.id.frame_loading_fragmentFrame);

        //backgroundエラー確認
        String report = InternalBackgroundErrorReportManager.get();
        if(report != null) {
            Logger.i("background report found.");
            Intent reportIntent = new Intent(this, ReportActivity.class);
            reportIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            reportIntent.putExtra(Constants.IntentExtraKey.REPORT_BODY, report);
            startActivity(reportIntent);
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
            } else {
                Logger.i("internal tentative account does not exist.");
                Intent loginIntent = new Intent(this, LoginActivity.class);
                loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(loginIntent);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        DataBaseAccessor.removeLoadFragment(this);
    }

    private final class RegisteredAccountExistenceCallBack extends GetAccessCallBack<Boolean> {

        public RegisteredAccountExistenceCallBack(GetRequest<Boolean> getRequest) {
            super(getRequest);
        }

        @Override
        public void onSuccess(@NotNull Boolean existsAccount) {
            if(existsAccount) {
                Logger.i("registered found.");
                Intent intent = new Intent(InitActivity.this, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            } else {
                Logger.i("registered not found.");
                Toast.makeText(InitActivity.this, R.string.toast_inquiry_notFound, Toast.LENGTH_LONG).show();
                Intent intent = new Intent(InitActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        }

        @Override
        public void onFailure() {
            Toast.makeText(InitActivity.this, R.string.toast_failure_closeApp, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onTimeOut() {
            retry();
            Toast.makeText(InitActivity.this, R.string.toast_failure_wait, Toast.LENGTH_LONG).show();
            Intent loginIntent = new Intent(InitActivity.this, LoginActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            InitActivity.this.startActivity(loginIntent);
        }
    }

    private final class TentativeAccountExistenceCallBack extends GetAccessCallBack<Boolean> {

        public TentativeAccountExistenceCallBack(GetRequest<Boolean> getRequest) {
            super(getRequest);
        }

        @Override
        public void onSuccess(@NotNull Boolean existsAccount) {
            if(existsAccount) {
                Intent authIntent = new Intent(InitActivity.this, AuthorizationActivity.class);
                authIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                authIntent.putExtra(Constants.IntentExtraKey.ACCOUNT_MAIL, InternalTentativeAccountManager.getMailAddress());
                authIntent.putExtra(Constants.IntentExtraKey.ACCOUNT_ENCRYPTED_PASSWORD, InternalTentativeAccountManager.getEncryptedPassword());
                InitActivity.this.startActivity(authIntent);
            } else {
                InternalTentativeAccountManager.remove();
            }
        }

        @Override
        public void onFailure() {
            Toast.makeText(InitActivity.this, R.string.toast_failure_wait, Toast.LENGTH_LONG).show();
            Intent loginIntent = new Intent(InitActivity.this, LoginActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            InitActivity.this.startActivity(loginIntent);
        }

        @Override
        public void onTimeOut() {
            retry();
            Toast.makeText(KozuZen.getInstance(), KozuZen.getInstance().getString(R.string.toast_failure_wait), Toast.LENGTH_LONG).show();
            Intent loginIntent = new Intent(KozuZen.getInstance(), LoginActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            KozuZen.getInstance().startActivity(loginIntent);
        }
    }
}