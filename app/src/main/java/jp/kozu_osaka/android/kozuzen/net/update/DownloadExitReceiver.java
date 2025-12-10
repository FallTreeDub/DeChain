package jp.kozu_osaka.android.kozuzen.net.update;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.io.File;

import jp.kozu_osaka.android.kozuzen.Constants;
import jp.kozu_osaka.android.kozuzen.KozuZen;
import jp.kozu_osaka.android.kozuzen.R;
import jp.kozu_osaka.android.kozuzen.annotation.RequireIntentExtra;
import jp.kozu_osaka.android.kozuzen.util.NotificationProvider;

/**
 * 何らかの理由でダウンロードが停止、終了した際に呼び出されるBroadcastReceiver。
 */
@RequireIntentExtra(extraClazz = Integer.class, extraKey = Constants.IntentExtraKey.RECEIVER_EXIT_CODE)
/**
 * インストール成功時
 */
@RequireIntentExtra(extraClazz = Integer.class, extraKey = Constants.IntentExtraKey.RECEIVER_EXIT_SESSION_ID)
@RequireIntentExtra(extraClazz = String.class, extraKey = Constants.IntentExtraKey.RECEIVER_EXIT_APK_PATH)
public final class DownloadExitReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        int exitCode = intent.getIntExtra(Constants.IntentExtraKey.RECEIVER_EXIT_CODE, -1);
        if(!DeChainUpDater.UpDaterStatus.isValid(exitCode)) {
            KozuZen.createErrorReport(new IllegalArgumentException("ExitCode is invalid:" + exitCode));
            DeChainUpDater.setStatus(context, DeChainUpDater.UpDaterStatus.STATUS_FAILED);
        }
        DeChainUpDater.UpDaterStatus status = DeChainUpDater.UpDaterStatus.from(exitCode);

        if(status == DeChainUpDater.UpDaterStatus.STATUS_SUCCESS) {
            int sessionID = intent.getIntExtra(Constants.IntentExtraKey.RECEIVER_EXIT_SESSION_ID, -1);
            if(sessionID == -1) {
                KozuZen.createErrorReport(new IllegalArgumentException("SessionID is -1."));
                return;
            }
            String installedAPKPath = intent.getStringExtra(Constants.IntentExtraKey.RECEIVER_EXIT_APK_PATH);
            if(installedAPKPath == null) {
                KozuZen.createErrorReport(new IllegalArgumentException("Installed APK Path is null."));
                return;
            }

            DeChainUpDater.saveInstallingInfo(context, sessionID, installedAPKPath);
            if(KozuZen.getCurrentActivity() != null) { //アプリがフォアグラウンドの場合
                try {
                    DeChainUpDater.showUpdateRequestDialog(context, new File(installedAPKPath), sessionID);
                } catch(Exception e) {
                    KozuZen.createErrorReport(e);
                }
            } else {
                NotificationProvider.sendNotification(
                        context.getString(R.string.notification_title_update_success),
                        NotificationProvider.NotificationIcon.DECHAIN_APP_ICON,
                        context.getString(R.string.notification_message_update_success)
                );
            }
        }
        DeChainUpDater.setStatus(context, status);
    }
}
