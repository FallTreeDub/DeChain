package jp.kozu_osaka.android.kozuzen;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;

import jp.kozu_osaka.android.kozuzen.access.AccessThread;
import jp.kozu_osaka.android.kozuzen.access.DeChainSpreadSheet;
import jp.kozu_osaka.android.kozuzen.access.ServiceAccount;
import jp.kozu_osaka.android.kozuzen.access.task.foreground.InquiryTask;
import jp.kozu_osaka.android.kozuzen.access.task.foreground.TentativeInquiryTask;
import jp.kozu_osaka.android.kozuzen.internal.InternalRegisteredAccount;
import jp.kozu_osaka.android.kozuzen.internal.InternalTentativeAccount;
import jp.kozu_osaka.android.kozuzen.notification.NotificationProvider;

/**
 * アプリの起動時に実行される。
 * 内部アカウントの照会を行い、その状態に応じて次に表示する画面を決定する。
 */
public final class SprashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sprash);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Spreadsheet用意
        try {
            Log.i(Constants.Debug.LOGNAME_INFO, "SpreadSheet init");
            DeChainSpreadSheet.init(ServiceAccount.get());
        } catch (Exception e) {
            KozuZen.createErrorReport(this, e);
        }

        //通知送信のリクエストを表示
        NotificationProvider.requestNotification(this);

        //AlarmManagerの動作権限をリクエストするDialogを表示
        //todo: 表示前に切り替わるときを想定、windowLeakが起きているので改善必要
        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                && !alarmManager.canScheduleExactAlarms()) {
            //権限をリクエスト
            new AlertDialog.Builder(this)
                    .setTitle(R.string.dialog_request_exactAlarm_title)
                    .setMessage(R.string.dialog_request_exactAlarm_body)
                    .setPositiveButton(R.string.dialog_request_exactAlarm_button_yes, (dialog, which) -> {
                        Intent settingIntent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                        startActivity(settingIntent);
                    })
                    .setNegativeButton(R.string.dialog_request_exactAlarm_button_no, (dialog, which) -> {})
                    .create()
                    .show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        //todo: アプリ閉じている間にworkerがエラー起こしてないか確認
        // WorkerErrorReport registeredReport = null;
        // try {
        //     registeredReport = WorkerErrorReport.Manager.getRegistered();
        // } catch(IOException e) {
        //     KozuZen.createErrorReport(this, e);
        // }
        // if(registeredReport != null) {
        //     Intent reportIntent = new Intent(this, ReportActivity.class);
        //     reportIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        //     reportIntent.putExtra(Constants.IntentExtraKey.REPORT_BODY, registeredReport.getBody());
        //     startActivity(reportIntent);
        // }

        //ログイン状況確認
        InternalRegisteredAccount internalRegisteredAccount = InternalRegisteredAccount.get();
        if(internalRegisteredAccount != null) { //ログイン済みとして登録されたアカウントがある場合
            Log.i(Constants.Debug.LOGNAME_INFO, "internal account exists.");
            AccessThread accessThread = new AccessThread(
                    new InquiryTask(
                            this,
                            R.id.frame_sprash_fragmentFrame,
                            internalRegisteredAccount.getMailAddress(),
                            internalRegisteredAccount.getEncryptedPassword())
            );
            accessThread.start();
        } else {
            //仮登録内部アカウント取得
            Log.i(Constants.Debug.LOGNAME_INFO, "internal account does not exist.");
            InternalTentativeAccount internalTentative = InternalTentativeAccount.get();

            //仮登録内部アカウントが存在する場合
            if(internalTentative != null) {
                Log.i(Constants.Debug.LOGNAME_INFO, "internal tentative account exists.");
                AccessThread accessThread = new AccessThread(
                        new TentativeInquiryTask(
                                this,
                                R.id.frame_sprash_fragmentFrame,
                                internalTentative.getMailAddress(),
                                internalTentative.getEncryptedPassword()
                        )
                );
                accessThread.start();
            } else {
                Log.i(Constants.Debug.LOGNAME_INFO, "internal tentative account does not exist.");
                Intent loginIntent = new Intent(this, LoginActivity.class);
                loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(loginIntent);
            }
        }
    }
}