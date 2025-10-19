package jp.kozu_osaka.android.kozuzen;

import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import jp.kozu_osaka.android.kozuzen.access.DataBaseAccessor;
import jp.kozu_osaka.android.kozuzen.util.DialogProvider;
import jp.kozu_osaka.android.kozuzen.util.PermissionsStatus;

/**
 * DeChain起動時に表示される。
 * デバイスの状態やアプリに対する権限を確認する。
 */
public final class CheckPermissionsActivity extends AppCompatActivity {

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

        //ロード画面表示(fragment)
        DataBaseAccessor.showLoadFragment(this, R.id.frame_loading_fragmentFrame);

        //現状の権限取得状況を確認
        if(!PermissionsStatus.isAllowedAppUsageStats()) {
            requestAppUsageStatsPermission();
        }
        if(!PermissionsStatus.isAllowedNotification()) {
            requestNotificationPermission();
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if(!PermissionsStatus.isAllowedScheduleAlarm()) {
                requestExactAlarms();
            }
        }
        if(!PermissionsStatus.isAllowedInstallPackage()) {
            requestInstallPackages();
        }

        Intent intent = new Intent(this, InitActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        DataBaseAccessor.removeLoadFragment(this);
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

    @RequiresApi(Build.VERSION_CODES.S)
    private void requestExactAlarms() {
        DialogProvider.makeBuilder(this, R.string.dialog_request_title, R.string.dialog_request_exactAlarm_body)
                .setNegativeButton(R.string.dialog_request_button_no, (dialog, which) -> {
                    dialog.dismiss();
                })
                .setPositiveButton(R.string.dialog_request_button_yes, (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                    intent.setData(Uri.parse("package:" + this.getPackageName()));
                    startActivity(intent);
                    dialog.dismiss();
                })
                .create()
                .show();
    }

    private void requestInstallPackages() {
        DialogProvider.makeBuilder(this, R.string.dialog_request_title, R.string.dialog_request_installPackages_body)
                .setNegativeButton(R.string.dialog_request_button_no, (dialog, which) -> {
                    dialog.dismiss();
                })
                .setPositiveButton(R.string.dialog_request_button_yes, (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
                    intent.setData(Uri.parse("package:" + this.getPackageName()));
                    startActivity(intent);
                    dialog.dismiss();
                })
                .create()
                .show();
    }
}