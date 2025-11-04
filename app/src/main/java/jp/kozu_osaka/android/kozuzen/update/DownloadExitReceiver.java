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

        switch(exitCode) {
            case DeChainUpDater.STATUS_FAILED:
                break;
            case DeChainUpDater.STATUS_SUCCESS:
                int sessionID = intent.getIntExtra(Constants.IntentExtraKey.RECEIVER_EXIT_CODE, -1);
                if(sessionID == -1) {
                    KozuZen.createErrorReport(new IllegalArgumentException("SessionID is -1."));
                    return;
                }
                String installedAPKPath = intent.getStringExtra(Constants.IntentExtraKey.RECEIVER_EXIT_APK_PATH);
                if(installedAPKPath == null) {
                    KozuZen.createErrorReport(new IllegalArgumentException("Installed APK Path is null."));
                    return;
                }

                if(KozuZen.getCurrentActivity() != null) { //アプリがフォアグラウンドの場合
                    PackageInstaller installer = context.getPackageManager().getPackageInstaller();
                    try(PackageInstaller.Session session = installer.openSession(sessionID)) {
                        Intent installIntent = new Intent(context, KozuZen.getCurrentActivity());
                        Uri contentUri = FileProvider.getUriForFile(
                                context,
                                KozuZen.getInstance().getPackageName() + ".provider",
                                new File(installedAPKPath)
                        );
                        installIntent.setDataAndType(
                                contentUri,
                                "application/vnd.android.package-archive"
                        );
                        installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        PendingIntent pi = PendingIntent.getBroadcast(
                                context, 0, installIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE
                        );
                        session.commit(pi.getIntentSender());
                    } catch(IOException e) {
                        KozuZen.createErrorReport(e);
                    }
                } else {

                }
                break;
            default:
                KozuZen.createErrorReport(new IllegalArgumentException("ExitCode is invalid:" + exitCode));
                break;
        }
    }
}
