package jp.kozu_osaka.android.kozuzen.net.usage;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.Locale;

import jp.kozu_osaka.android.kozuzen.KozuZen;
import jp.kozu_osaka.android.kozuzen.internal.InternalRegisteredAccountManager;
import jp.kozu_osaka.android.kozuzen.util.Logger;
import jp.kozu_osaka.android.kozuzen.util.NotificationProvider;

/**
 * 毎日夜20時に、SNSとゲームアプリの使用時間を集計するための{@link UsageDataSendService}を起動させる。
 */
public final class UsageDataBroadcastReceiver extends BroadcastReceiver {

    private static final int ALARM_REQUEST_CODE = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
        if(!InternalRegisteredAccountManager.isRegistered()) { //アカウント削除時にはpendはキャンセルされているので、この時点でregisterされていないのはおかしい
            KozuZen.createErrorReport(new IllegalStateException("Not found a internal register account for registering a background error report."));
            return;
        }

        //処理用Service起動
        Intent serviceIntent = new Intent(context, UsageDataSendService.class);
        context.startForegroundService(serviceIntent);

        //次の20時へのpending
        pendThis(context);
    }

    @SuppressWarnings("ScheduleExactAlarm")
    public static void pendThis(@NotNull Context context) { //todo

        /*Calendar now = Calendar.getInstance();
        Calendar next8PM = new Calendar.Builder().setTimeOfDay(20, 0, 0).build();
        if(now.after(next8PM)) {
            next8PM.add(Calendar.DAY_OF_MONTH, 1);
        }
        
        Intent intent = new Intent(context, UsageDataBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, UsageDataBroadcastReceiver.ALARM_REQUEST_CODE, intent, PendingIntent.FLAG_IMMUTABLE);
        AlarmManager manager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next8PM.getTimeInMillis(), pendingIntent);*/

        AlarmManager manager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, UsageDataBroadcastReceiver.class);
        intent.setAction("jp.kozu_osaka.android.kozuzen.USAGE_DATA_ALARM");
        intent.setPackage(context.getPackageName());
        PendingIntent forCreating = PendingIntent.getBroadcast(context, UsageDataBroadcastReceiver.ALARM_REQUEST_CODE, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        Calendar now = Calendar.getInstance(Locale.JAPAN);
        now.add(Calendar.MINUTE, 1);
        manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, now.getTimeInMillis(), forCreating);
        Logger.i("pended.");
    }

    public static void cancelThis(Context context) {
        Intent intent = new Intent(context, UsageDataBroadcastReceiver.class);
        intent.setAction("jp.kozu_osaka.android.kozuzen.USAGE_DATA_ALARM");
        intent.setPackage(context.getPackageName());
        AlarmManager manager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, UsageDataBroadcastReceiver.ALARM_REQUEST_CODE, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        manager.cancel(pendingIntent);
        pendingIntent.cancel();
    }
}
