package jp.kozu_osaka.android.kozuzen.net.sendusage;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;

import jp.kozu_osaka.android.kozuzen.KozuZen;
import jp.kozu_osaka.android.kozuzen.exception.NotAllowedPermissionException;
import jp.kozu_osaka.android.kozuzen.util.PermissionsStatus;
import jp.kozu_osaka.android.kozuzen.R;
import jp.kozu_osaka.android.kozuzen.internal.InternalRegisteredAccountManager;
import jp.kozu_osaka.android.kozuzen.exception.NotFoundInternalAccountException;
import jp.kozu_osaka.android.kozuzen.util.NotificationProvider;

public final class UsageDataBroadcastReceiver extends BroadcastReceiver {

    private static final int ALARM_REQUEST_CODE = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
        //権限確認
        if(!(PermissionsStatus.isAllowedNotification() && PermissionsStatus.isAllowedScheduleAlarm() && PermissionsStatus.isAllowedAppUsageStats())) {
            NotificationProvider.sendNotification(
                    NotificationProvider.NotificationTitle.ON_BACKGROUND_ERROR_OCCURRED,
                    NotificationProvider.NotificationIcon.NONE,
                    R.string.notification_message_background_noPermission
            );
            return;
        }

        if(!InternalRegisteredAccountManager.isRegistered()) {
            KozuZen.createErrorReport(new NotFoundInternalAccountException("Not found a internal register account for registering a background error report."));
            return;
        }

        Intent serviceIntent = new Intent(context, UsageDataSendService.class);
        context.startService(serviceIntent);

        try {
            pendThis(context);
        } catch (NotAllowedPermissionException e) {
            KozuZen.createErrorReport(e);
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    public static void pendThis(@NotNull Context context) throws NotAllowedPermissionException {
        if(!PermissionsStatus.isAllowedScheduleAlarm()) {
            throw new NotAllowedPermissionException("Scheduling alarm is not allowed.", Manifest.permission.SCHEDULE_EXACT_ALARM);
        }

        Calendar now = Calendar.getInstance();
        Calendar next8PM = new Calendar.Builder().setTimeOfDay(20, 0, 0).build();
        if(now.after(next8PM)) {
            next8PM.add(Calendar.DAY_OF_MONTH, 1);
        }
        
        Intent intent = new Intent(context, UsageDataBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, UsageDataBroadcastReceiver.ALARM_REQUEST_CODE, intent, PendingIntent.FLAG_IMMUTABLE);
        AlarmManager manager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next8PM.getTimeInMillis(), pendingIntent);
    }

    public static void cancelThis(Context context) {
        Intent intent = new Intent(context, UsageDataBroadcastReceiver.class);
        AlarmManager manager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, UsageDataBroadcastReceiver.ALARM_REQUEST_CODE, intent, PendingIntent.FLAG_IMMUTABLE);
        manager.cancel(pendingIntent);
    }
}
