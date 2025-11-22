package jp.kozu_osaka.android.kozuzen.util;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Process;
import android.provider.Settings;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import jp.kozu_osaka.android.kozuzen.KozuZen;
import jp.kozu_osaka.android.kozuzen.R;

/**
 * AndroidデバイスのDeChainに対する権限を確認する。
 */
public final class PermissionsStatus {

    private PermissionsStatus() {}

    public static boolean isAllowedNotification() {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true;
        return ContextCompat.checkSelfPermission(KozuZen.getInstance(), android.Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isAllowedAppUsageStats() {
        AppOpsManager aoManager = (AppOpsManager)KozuZen.getInstance().getSystemService(Context.APP_OPS_SERVICE);
        int mode = aoManager.unsafeCheckOp(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), KozuZen.getInstance().getPackageName());
        if(mode != AppOpsManager.MODE_DEFAULT) return true;
        return KozuZen.getInstance().checkPermission("android.permission.PACKAGE_USAGE_STATS", android.os.Process.myPid(), Process.myUid())
                == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isAllowedInstallPackage() {
        return KozuZen.getInstance().getPackageManager().canRequestPackageInstalls();
    }

    public static boolean isAllowedScheduleAlarm() {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true;
        AlarmManager am = (AlarmManager)KozuZen.getInstance().getSystemService(Context.ALARM_SERVICE);
        return am.canScheduleExactAlarms();
    }

    public static AlertDialog createDialogNotification(Context context) {
        return DialogProvider.makeBuilder(context, R.string.dialog_request_title, R.string.dialog_request_notification_body)
                .setNegativeButton(R.string.dialog_request_button_no, (dialog, which) -> {
                    dialog.dismiss();
                })
                .setPositiveButton(R.string.dialog_request_button_yes, (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                    intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
                    context.startActivity(intent);
                    dialog.dismiss();
                })
                .create();
    }

    public static AlertDialog createDialogAppUsageStats(Context context) {
        return DialogProvider.makeBuilder(context, R.string.dialog_request_title, R.string.dialog_request_usageStats_body)
                .setNegativeButton(R.string.dialog_request_button_no, (dialog, which) -> {
                    dialog.dismiss();
                })
                .setPositiveButton(R.string.dialog_request_button_yes, (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                    intent.setData(Uri.parse("package:" + context.getPackageName()));
                    context.startActivity(intent);
                    dialog.dismiss();
                })
                .create();
    }

    @RequiresApi(Build.VERSION_CODES.S)
    public static AlertDialog createDialogExactAlarm(Context context) {
        return DialogProvider.makeBuilder(context, R.string.dialog_request_title, R.string.dialog_request_exactAlarm_body)
                .setNegativeButton(R.string.dialog_request_button_no, (dialog, which) -> {
                    dialog.dismiss();
                })
                .setPositiveButton(R.string.dialog_request_button_yes, (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                    intent.setData(Uri.parse("package:" + context.getPackageName()));
                    context.startActivity(intent);
                    dialog.dismiss();
                })
                .create();
    }

    public static AlertDialog createDialogInstallPackages(Context context, Runnable callBackOnPositive, Runnable callBackOnNegative) {
        return DialogProvider.makeBuilder(context, R.string.dialog_request_title, R.string.dialog_request_installPackages_body)
                .setNegativeButton(R.string.dialog_request_button_no, (dialog, which) -> {
                    callBackOnNegative.run();
                    dialog.dismiss();
                })
                .setPositiveButton(R.string.dialog_request_button_yes, (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
                    intent.setData(Uri.parse("package:" + context.getPackageName()));
                    context.startActivity(intent);
                    callBackOnPositive.run();
                    dialog.dismiss();
                })
                .create();
    }
}
