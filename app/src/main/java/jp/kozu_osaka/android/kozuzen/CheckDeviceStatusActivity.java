package jp.kozu_osaka.android.kozuzen;

import android.app.AlarmManager;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.provider.Settings;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import jp.kozu_osaka.android.kozuzen.access.DataBaseAccessor;
import jp.kozu_osaka.android.kozuzen.util.DialogProvider;
import jp.kozu_osaka.android.kozuzen.util.Logger;

/**
 * DeChain起動時に表示される。
 * デバイスの状態やアプリに対する権限を確認する。
 */
public final class CheckDeviceStatusActivity extends AppCompatActivity {

    private final CheckStatusViewModel STATUS_VIEWMODEL = new CheckStatusViewModel();

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

        if(canInstallUnknownApps()) {
            DialogProvider.makeBuilder(this, R.string.dialog_unknownApp_title, R.string.dialog_unknownApp_body)
                    .setNegativeButton(R.string.dialog_unknownApp_button_no, (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .setPositiveButton(R.string.dialog_unknownApp_button_yes, (dialog, which) -> {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                                Uri.parse("package:" + getPackageName()));
                        startActivity(intent);
                        dialog.dismiss();
                    })
                    .create()
                    .show();
        }

        //現状の権限取得状況を確認
        if(isAllowedAppUsageStats()) STATUS_VIEWMODEL.doneAppUsage();
        if(isAllowedNotification()) STATUS_VIEWMODEL.doneNotification();

        if(!STATUS_VIEWMODEL.nowStatus.hasObservers()) {
            STATUS_VIEWMODEL.nowStatus.observe(this, value -> {
                request();
            });
        }
        if(STATUS_VIEWMODEL.isFirstRequest()) {
            request();
            STATUS_VIEWMODEL.setFalseFirstRequest();
        }
    }

    private boolean canInstallUnknownApps() {
        return getPackageManager().canRequestPackageInstalls();
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
                    STATUS_VIEWMODEL.doneNotification();
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
                    STATUS_VIEWMODEL.doneAppUsage();
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

    private void request() {
        if(STATUS_VIEWMODEL.nowStatus.getValue() == 0b11) {
            Intent intent = new Intent(this, InitActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }

        if((STATUS_VIEWMODEL.nowStatus.getValue() & CheckStatusViewModel.DONE_NOTIFICATION) == 0b00) {
            requestNotificationPermission();
        }
        if((STATUS_VIEWMODEL.nowStatus.getValue() & CheckStatusViewModel.DONE_APPUSAGE) == 0b00) {
            requestAppUsageStatsPermission();
        }
    }

    private static class CheckStatusViewModel extends ViewModel {

        public static final int DONE_APPUSAGE = 0b01;
        public static final int DONE_NOTIFICATION = 0b10;


        private final MutableLiveData<Integer> nowStatus = new MutableLiveData<>(0x00);
        private final MutableLiveData<Boolean> isFirstRequest = new MutableLiveData<>(true);

        public CheckStatusViewModel() {}

        public void doneAppUsage() {
            nowStatus.setValue(nowStatus.getValue() | DONE_APPUSAGE);
            Logger.i(nowStatus.getValue());
        }

        public void doneNotification() {
            nowStatus.setValue(nowStatus.getValue() | DONE_NOTIFICATION);
            Logger.i(nowStatus.getValue());
        }

        public void setFalseFirstRequest() {
            isFirstRequest.setValue(false);
        }

        public boolean isFirstRequest() {
            return isFirstRequest.getValue();
        }
    }
}