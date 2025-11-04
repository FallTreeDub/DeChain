package jp.kozu_osaka.android.kozuzen.update;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.os.IBinder;

import androidx.core.util.Preconditions;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import jp.kozu_osaka.android.kozuzen.Constants;
import jp.kozu_osaka.android.kozuzen.KozuZen;
import jp.kozu_osaka.android.kozuzen.R;
import jp.kozu_osaka.android.kozuzen.annotation.RequireIntentExtra;
import jp.kozu_osaka.android.kozuzen.util.NotificationProvider;

/**
 * ダウンロードしたAPKファイルを実際にインストールするService。
 */
@RequireIntentExtra(extraClazz = String.class, extraKey = Constants.IntentExtraKey.UPDATE_INSTALLED_APK_PATH)
public final class InstallService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startID) {
        String installedAPKPath = intent.getStringExtra(Constants.IntentExtraKey.UPDATE_INSTALLED_APK_PATH);
        if(installedAPKPath == null) {
            sendExitReceiver(DeChainUpDater.STATUS_FAILED);
            KozuZen.createErrorReport(new IllegalArgumentException("The installed apk path is null."));
            return START_STICKY;
        };
        startForeground(startID, NotificationProvider.buildNotification(this, NotificationProvider.NotificationIcon.NONE, R.string.notification_update_title, R.string.notification_update_desc));

        PackageInstaller installer = getPackageManager().getPackageInstaller();
        PackageInstaller.SessionParams params =
                new PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL);
        try {
            int sessionId = installer.createSession(params);
            try(PackageInstaller.Session session = installer.openSession(sessionId);
                OutputStream out = session.openWrite("latest.apk", 0, -1);
                InputStream in = new FileInputStream(installedAPKPath)) {
                byte[] buffer = new byte[8192];
                int len;
                while((len = in.read(buffer)) != -1) out.write(buffer, 0, len);
                session.fsync(out);
            } catch(Exception e) {
                sendExitReceiver(DeChainUpDater.STATUS_FAILED);
                KozuZen.createErrorReport(e);
                return START_STICKY;
            }

            Intent doneIntent = new Intent(Constants.IntentAction.UPDATE_EXIT);
            doneIntent.putExtra(Constants.IntentExtraKey.RECEIVER_EXIT_CODE, DeChainUpDater.STATUS_SUCCESS);
            doneIntent.putExtra(Constants.IntentExtraKey.RECEIVER_EXIT_SESSION_ID, sessionId);
            doneIntent.putExtra(Constants.IntentExtraKey.RECEIVER_EXIT_APK_PATH, installedAPKPath);
            sendBroadcast(doneIntent);
        } catch(IOException e) {
            sendExitReceiver(DeChainUpDater.STATUS_FAILED);
            KozuZen.createErrorReport(e);
            stopForeground(true);
            return START_STICKY;
        }
        return START_STICKY;
    }

    private void sendExitReceiver(int status) {
        Intent errorIntent = new Intent(Constants.IntentAction.UPDATE_EXIT);
        errorIntent.putExtra(Constants.IntentExtraKey.RECEIVER_EXIT_CODE, status);
        sendBroadcast(errorIntent);
    }
}