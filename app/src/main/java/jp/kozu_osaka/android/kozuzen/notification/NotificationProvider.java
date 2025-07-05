package jp.kozu_osaka.android.kozuzen.notification;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.StringRes;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import jp.kozu_osaka.android.kozuzen.KozuZen;
import jp.kozu_osaka.android.kozuzen.R;

/**
 * 通知を作成、送信するクラス。
 */
public final class NotificationProvider {

    private static final String CHANNEL_ID = "channel_notification_kozuzen";
    private static final int NOTIFICATION_ID = 800;

    private NotificationProvider() {}

    /**
     * SDKバージョンが、通知送信権限のリクエストが必要である33以上である場合に通知送信リクエスト画面を表示する。
     * すでに通知が許可されている場合は無視される。
     * @param activity リクエストウィンドウを表示させるactivity。
     */
    public static void requestNotification(Activity activity) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { //APIレベル33以上の時
            if(ContextCompat.checkSelfPermission(activity, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        1);
            }
        }
    }

    /**
     * {@code message}を通知として送信する。
     * APIレベル33以上の場合、通知権限のリクエストが必要であるため、{@link NotificationProvider#requestNotification(Activity activity)}
     * を実行する必要がある。リクエストが承認されていない場合、このメソッドの実行は無視される。
     * @param title タイトル。
     * @param message 通知の本文。
     */
    public static void sendNotification(NotificationTitle title, String message) {
        //リクエスト承認されていない場合は無視
        if(ContextCompat.checkSelfPermission(KozuZen.getInstance(), android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) return;

        //通知作成
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                KozuZen.getInstance().getString(R.string.app_name),
                NotificationManager.IMPORTANCE_DEFAULT
        );
        NotificationManager manager = KozuZen.getInstance().getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);
        Notification notification = new NotificationCompat.Builder(KozuZen.getInstance(), CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentTitle(title.getTitle())
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentText(message)
                .build();

        //通知送信
        NotificationManagerCompat.from(KozuZen.getInstance())
                .notify(NOTIFICATION_ID, notification);
    }

    /**
     * @see NotificationProvider#sendNotification(NotificationTitle, String)
     * @param title タイトル
     * @param messageId メッセージのid。
     */
    public static void sendNotification(NotificationTitle title, @StringRes int messageId) {
        sendNotification(title, KozuZen.getInstance().getString(messageId));
    }

    /**
     * 通知に設定するタイトル。
     */
    public enum NotificationTitle {

        /**
         * SpreadSheetへのアクセスに成功したとき。
         */
        ON_ACCESS_SUCCEEDED(R.string.notification_title_access_success),

        /**
         * SpreadSheetへのアクセスに失敗したとき。
         */
        ON_ACCESS_FAILED(R.string.notification_title_access_fail),

        /**
         * SpreadSheetへのアクセス処理でタイムアウトしたとき。
         */
        ON_ACCESS_TIMEOUT(R.string.notification_title_access_timeout),

        /**
         * バックグラウンド処理でエラーが発生したとき。
         */
        ON_BACKGROUND_ERROR_OCCURRED(R.string.notification_title_background_error);

        @StringRes
        private final int ID;

        NotificationTitle(@StringRes int id) {
            this.ID = id;
        }

        public String getTitle() {
            return KozuZen.getInstance().getString(this.ID);
        }
    }
}
