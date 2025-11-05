package jp.kozu_osaka.android.kozuzen.update;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.net.Uri;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;

import jp.kozu_osaka.android.kozuzen.Constants;
import jp.kozu_osaka.android.kozuzen.KozuZen;
import jp.kozu_osaka.android.kozuzen.annotation.RequireIntentExtra;
import jp.kozu_osaka.android.kozuzen.util.Logger;

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

        Logger.i("aiueo700..." + intent.getIntExtra(Constants.IntentExtraKey.RECEIVER_EXIT_SESSION_ID, -1) + ", " + intent.getStringExtra(Constants.IntentExtraKey.RECEIVER_EXIT_APK_PATH));

        if(status == DeChainUpDater.UpDaterStatus.STATUS_SUCCESS) {
            Logger.i("aiueo71...");
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

            Logger.i("aiueo71as...");

            DeChainUpDater.saveInstallingInfo(context, sessionID, installedAPKPath);
            if(KozuZen.getCurrentActivity() != null) { //アプリがフォアグラウンドの場合
                Logger.i("fore...");
                try {
                    DeChainUpDater.showUpdateRequestDialog(context, new File(installedAPKPath), sessionID);
                } catch(Exception e) {
                    KozuZen.createErrorReport(e);
                }
            }
        }
        DeChainUpDater.setStatus(context, status);
    }
}
