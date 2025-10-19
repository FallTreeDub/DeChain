package jp.kozu_osaka.android.kozuzen.util;

import android.app.AlarmManager;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Process;

import androidx.core.content.ContextCompat;

import jp.kozu_osaka.android.kozuzen.KozuZen;

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
}
