package jp.kozu_osaka.android.kozuzen;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
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
import jp.kozu_osaka.android.kozuzen.util.Logger;

/**
 * アプリの起動時に実行される。
 * 内部アカウントの照会を行い、その状態に応じて次に表示する画面を決定する。
 */
public final class SprashActivity extends AppCompatActivity {

    private int permissionProgress = 0;

    private static final int PERMISSION_PROGRESS_NOTIFICATION_PERMITTED = 2;
    private static final int PERMISSION_PROGRESS_ALARM_PERMITTED = 4;
    private static final int PERMISSION_PROGRESS_APPUSAGE_PERMITTED = 8;

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

        //通知送信のリクエストを表示
        NotificationProvider.requestNotification(this);

        if(!checkAppUsagePermission()) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.PACKAGE_USAGE_STATS},
                    1);
            // Intent settingIntent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            // startActivity(settingIntent);
        }

        //AlarmManagerの動作権限をリクエストするDialogを表示
        //todo: 表示前に切り替わるときを想定、windowLeakが起きているので改善必要
        if(!checkAlarmManagerPermission()) {
            //権限をリクエスト
            new AlertDialog.Builder(this)
                    .setTitle(R.string.dialog_request_exactAlarm_title)
                    .setMessage(R.string.dialog_request_exactAlarm_body)
                    .setCancelable(false)
                    .setPositiveButton(R.string.dialog_request_exactAlarm_button_yes, (dialog, which) -> {
                        @SuppressLint("InlinedApi") //checkAlarmManagerPermission()がfalseを返すのはAPILevel31以上の時のみのため、SuppressWarningを行う。
                        Intent settingIntent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                        settingIntent.setData(Uri.parse("package:" + this.getPackageName()));
                        startActivity(settingIntent);
                        dialog.dismiss();
                    })
                    .setNegativeButton(R.string.dialog_request_exactAlarm_button_no, (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .setOnDismissListener((dialog) -> {

                    })
                    .create()
                    .show();
        }

        //Spreadsheet用意
        try {
            Logger.i("SpreadSheet init");
            DeChainSpreadSheet.init(ServiceAccount.get());
        } catch (Exception e) {
            KozuZen.createErrorReport(this, e);
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
            Logger.i("internal account exists.");
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
            Logger.i("internal account does not exist.");
            InternalTentativeAccount internalTentative = InternalTentativeAccount.get();

            //仮登録内部アカウントが存在する場合
            if(internalTentative != null) {
                Logger.i("internal tentative account exists.");
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
                Logger.i("internal tentative account does not exist.");
                Intent loginIntent = new Intent(this, LoginActivity.class);
                loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(loginIntent);
            }
        }
    }

    private boolean checkAppUsagePermission() {
        AppOpsManager manager = (AppOpsManager)this.getSystemService(Context.APP_OPS_SERVICE);
        int mode = manager.unsafeCheckOp(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), this.getPackageName());
        if(mode == AppOpsManager.MODE_DEFAULT) {
            return this.checkPermission("android.permission.PACKAGE_USAGE_STATS", android.os.Process.myPid(), android.os.Process.myUid())
                    == PackageManager.PERMISSION_GRANTED;
        }
        return (mode == AppOpsManager.MODE_ALLOWED);
    }

    private boolean checkAlarmManagerPermission() {
        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return alarmManager.canScheduleExactAlarms();
        }
        return true;
    }
}