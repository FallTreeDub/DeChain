package jp.kozu_osaka.android.kozuzen.background;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import jp.kozu_osaka.android.kozuzen.notification.NotificationProvider;

/**
 * バックグラウンド下で一日ごとに送る利用状況の通知をAlarmManagerから送信するためのBroadcastReceiver。
 */
public class DailyNotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //利用状況の取得

        NotificationProvider.sendNotification();
    }
}
