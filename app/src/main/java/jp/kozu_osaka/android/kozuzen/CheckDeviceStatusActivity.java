package jp.kozu_osaka.android.kozuzen;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.provider.Settings;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import jp.kozu_osaka.android.kozuzen.access.DataBaseAccessor;
import jp.kozu_osaka.android.kozuzen.background.UsageDataWorker;
import jp.kozu_osaka.android.kozuzen.internal.InternalRegisteredAccount;
import jp.kozu_osaka.android.kozuzen.util.DialogProvider;
import jp.kozu_osaka.android.kozuzen.util.Logger;

/**
 * DeChain起動時に表示される。
 * デバイスの状態やアプリに対する権限を確認する。
 */
public final class CheckDeviceStatusActivity extends AppCompatActivity {

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

        //ロード画面表示(fragment)
        DataBaseAccessor.showLoadFragment(this, R.id.frame_loading_launch_fragmentFrame);

        //現状の権限取得状況を確認
        if(!isAllowedAppUsageStats()) {
            requestAppUsageStatsPermission();
        }
        if(!isAllowedNotification()) {
            requestNotificationPermission();
        }

        //background(Worker)の動作状況
        if(InternalRegisteredAccount.get() != null) {
            if(!UsageDataWorker.isEnqueued(this)) {
                UsageDataWorker.enqueueToWorkManager(this);
                Logger.i("UsageDataWorker is registered.");
            }
        }

        Intent intent = new Intent(this, InitActivity.class);
        startActivity(intent);
    }

    private boolean isAllowedNotification() {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true;
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED;
    }

    private boolean isAllowedAppUsageStats() {
        AppOpsManager aoManager = (AppOpsManager)this.getSystemService(Context.APP_OPS_SERVICE);
        int mode = aoManager.unsafeCheckOp(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), this.getPackageName());
        if(mode != AppOpsManager.MODE_DEFAULT) return true;
        return this.checkPermission("android.permission.PACKAGE_USAGE_STATS", Process.myPid(), Process.myUid())
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestNotificationPermission() {
        DialogProvider.makeBuilder(this, R.string.dialog_request_title, R.string.dialog_request_notification_body)
                .setNegativeButton(R.string.dialog_request_button_no, (dialog, which) -> {
                    dialog.dismiss();
                })
                .setPositiveButton(R.string.dialog_request_button_yes, (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                    intent.putExtra(Settings.EXTRA_APP_PACKAGE, this.getPackageName());
                    startActivity(intent);
                    dialog.dismiss();
                })
                .create()
                .show();
    }

    private void requestAppUsageStatsPermission() {
        //AppUsageStatsの採取権限リクエスト
        DialogProvider.makeBuilder(this, R.string.dialog_request_title, R.string.dialog_request_usageStats_body)
                .setNegativeButton(R.string.dialog_request_button_no, (dialog, which) -> {
                    dialog.dismiss();
                })
                .setPositiveButton(R.string.dialog_request_button_yes, (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                    intent.setData(Uri.parse("package:" + this.getPackageName()));
                    startActivity(intent);
                    dialog.dismiss();
                })
                .create()
                .show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        DataBaseAccessor.removeLoadFragment(this);
    }
}