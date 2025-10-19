package jp.kozu_osaka.android.kozuzen.util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Icon;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import jp.kozu_osaka.android.kozuzen.KozuZen;
import jp.kozu_osaka.android.kozuzen.R;

/**
 * 簡素に通知を作成、送信するためのクラス。
 */
public final class NotificationProvider {

    private static final String CHANNEL_ID = "channel_notification_kozuzen";
    private static final int NOTIFICATION_ID = 800;

    private NotificationProvider() {}

    /**
     * {@code message}を通知として送信する。
     * APIレベル33以上の場合、通知権限のリクエストが必要である。リクエストが承認されていない場合、このメソッドの実行は無視される。
     * @param title タイトル。
     * @param message 通知の本文。
     */
    public static void sendNotification(NotificationTitle title, NotificationIcon icon, String message) {
        //権限承認されていない場合は無視
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
        Icon largeIcon = null;
        if(icon.getIconDrawable() != null) {
            largeIcon = Icon.createWithResource(KozuZen.getInstance(), icon.getIconDrawable());
        }
        Notification notification = new NotificationCompat.Builder(KozuZen.getInstance(), CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_name)
                .setLargeIcon(largeIcon)
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
     * @param title タイトル
     * @param messageId メッセージのid。
     * @see NotificationProvider#sendNotification(NotificationTitle, NotificationIcon, String)
     */
    public static void sendNotification(NotificationTitle title, NotificationIcon icon, @StringRes int messageId) {
        sendNotification(title, icon, KozuZen.getInstance().getString(messageId));
    }

    /**
     * 通知に設定するタイトル。
     */
    public enum NotificationTitle {

        /**
         * DataBaseへのアクセスに成功したとき。
         */
        ON_ACCESS_SUCCEEDED(R.string.notification_title_access_success),

        /**
         * DataBaseへのアクセスに失敗したとき。
         */
        ON_ACCESS_FAILED(R.string.notification_title_access_fail),

        /**
         * DataBaseへのアクセス処理でタイムアウトしたとき。
         */
        ON_ACCESS_TIMEOUT(R.string.notification_title_access_timeout),

        /**
         * バックグラウンド処理でエラーが発生したとき。
         */
        ON_BACKGROUND_ERROR_OCCURRED(R.string.notification_title_background_error),

        /**
         * 一日一回送る利用時間の通知において、他の被験者との比較で利用者が優れた成績であるとき。
         */
        DAILY_COMPARE_WITH_OTHER_SUPERIOR(R.string.notification_title_daily_compare_withOther_superior),

        /**
         * 一日一回送る利用時間の通知において、他の被験者との比較で利用者が劣った成績であるとき。
         */
        DAILY_COMPARE_WITH_OTHER_INFERIOR(R.string.notification_title_daily_compare_withOther_inferior),

        /**
         * 一日一回送る利用時間の通知において、先週の自分自身との比較で利用者が優れた成績であるとき。
         */
        DAILY_COMPARE_WITH_SELF_SUPERIOR(R.string.notification_title_daily_compare_withSelf_superior),

        /**
         * 一日一回送る利用時間の通知において、先週の自分自身との比較で利用者が劣った成績であるとき。
         */
        DAILY_COMPARE_WITH_SELF_INFERIOR(R.string.notification_title_daily_compare_withSelf_inferior);

        @StringRes
        private final int ID;

        NotificationTitle(@StringRes int id) {
            this.ID = id;
        }

        public String getTitle() {
            return KozuZen.getInstance().getString(this.ID);
        }
    }

    public enum NotificationIcon {

        NONE(null),

        DECHAIN_DUCK(R.drawable.notification_large_icon);

        @DrawableRes
        private final Integer iconDrawable;

        NotificationIcon(@DrawableRes Integer iconDrawable) {
            this.iconDrawable = iconDrawable;
        }

        @Nullable
        public Integer getIconDrawable() {
            return this.iconDrawable;
        }
    }
}
