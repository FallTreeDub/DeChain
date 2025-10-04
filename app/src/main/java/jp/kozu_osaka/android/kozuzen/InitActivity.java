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
import jp.kozu_osaka.android.kozuzen.access.request.get.GetTentativeExistenceRequest;
import jp.kozu_osaka.android.kozuzen.internal.InternalBackgroundErrorReport;
import jp.kozu_osaka.android.kozuzen.internal.InternalRegisteredAccount;
import jp.kozu_osaka.android.kozuzen.internal.InternalTentativeAccount;
import jp.kozu_osaka.android.kozuzen.util.Logger;

/**
 * 内部アカウントの有無、バックグラウンド時のエラー処理を行う。
 */
public final class InitActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading_launch);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Fragment表示
        DataBaseAccessor.showLoadFragment(this, R.id.frame_loading_launch_fragmentFrame);

        //backgroundエラー確認
        String report = InternalBackgroundErrorReport.get();
        if(report != null) {
            Logger.i("background report found.");
            Intent reportIntent = new Intent(this, ReportActivity.class);
            reportIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            reportIntent.putExtra(Constants.IntentExtraKey.REPORT_BODY, report);
            startActivity(reportIntent);
        }

        //ログイン状況確認
        InternalRegisteredAccount internalRegisteredAccount = InternalRegisteredAccount.get();
        if(internalRegisteredAccount != null) {
            //ログイン済みとして登録されたアカウントがある場合
            Logger.i("internal registered account exists.");
            RegisteredAccountExistenceCallBack callBack = new RegisteredAccountExistenceCallBack();
            DataBaseAccessor.sendGetRequest(new GetRegisteredExistenceRequest(
                    new GetRegisteredExistenceArguments(internalRegisteredAccount.getMailAddress(), internalRegisteredAccount.getEncryptedPassword())
                    ),
                    callBack
            );
        } else {
            //仮登録内部アカウント取得
            InternalTentativeAccount internalTentative = InternalTentativeAccount.get();

            //仮登録内部アカウントが存在する場合
            if(internalTentative != null) {
                Logger.i("internal tentative account exists.");
                TentativeAccountExistenceCallBack callback = new TentativeAccountExistenceCallBack();
                DataBaseAccessor.sendGetRequest(new GetTentativeExistenceRequest(
                        new GetTentativeExistenceArguments(internalTentative.getMailAddress()
                )), callback);
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
            //todo: エラー内容を表示させる？
        }

        @Override
        public void onTimeOut() {
            Toast.makeText(InitActivity.this, R.string.toast_timeout, Toast.LENGTH_LONG).show();
            Intent loginIntent = new Intent(InitActivity.this, LoginActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            InitActivity.this.startActivity(loginIntent);
        }
    }

    private final class TentativeAccountExistenceCallBack extends GetAccessCallBack<Boolean> {
        @Override
        public void onSuccess(@NotNull Boolean existsAccount) {
            if(existsAccount) {
                InternalTentativeAccount internalTentative = InternalTentativeAccount.get();
                Intent authIntent = new Intent(InitActivity.this, AuthorizationActivity.class);
                authIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                authIntent.putExtra(Constants.IntentExtraKey.ACCOUNT_MAIL, internalTentative.getMailAddress());
                authIntent.putExtra(Constants.IntentExtraKey.ACCOUNT_ENCRYPTED_PASSWORD, internalTentative.getEncryptedPassword());
                InitActivity.this.startActivity(authIntent);
            } else {
                InternalTentativeAccount.remove();
            }
        }

        @Override
        public void onFailure() {
            Intent loginIntent = new Intent(InitActivity.this, LoginActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            InitActivity.this.startActivity(loginIntent);
        }

        @Override
        public void onTimeOut() {
            Toast.makeText(KozuZen.getInstance(), KozuZen.getInstance().getString(R.string.toast_timeout), Toast.LENGTH_LONG).show();
            Intent loginIntent = new Intent(KozuZen.getInstance(), LoginActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            KozuZen.getInstance().startActivity(loginIntent);
        }
    }
}