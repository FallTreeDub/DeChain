package jp.kozu_osaka.android.kozuzen.net.update;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

import jp.kozu_osaka.android.kozuzen.Constants;
import jp.kozu_osaka.android.kozuzen.KozuZen;
import jp.kozu_osaka.android.kozuzen.R;
import jp.kozu_osaka.android.kozuzen.annotation.RequireIntentExtra;
import jp.kozu_osaka.android.kozuzen.util.NotificationProvider;

/**
 * <p>
 *     {@link DownloadService}でダウンロードしたZIPファイル形式のDeChainアプリのファイルをAPKにリネームし、
 *     Androidデバイスにインストールする。
 * </p>
 * <p>
 *     アップデートの詳しい処理の流れは{@code package-info.java}のjavadocへ。
 * </p>
 *
 * @see DeChainUpDater
 */
@RequireIntentExtra(extraClazz = String.class, extraKey = Constants.IntentExtraKey.UPDATE_INSTALLED_APK_PATH)
public final class InstallService extends Service {

    private PackageInstaller.Session nowSession = null;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startID) {
        if(DeChainUpDater.getStatus(this).equals(DeChainUpDater.UpDaterStatus.STATUS_STOPPING)) {
            stopSelf();
        }

        String installedAPKPath = intent.getStringExtra(Constants.IntentExtraKey.UPDATE_INSTALLED_APK_PATH);
        if(installedAPKPath == null) {
            exit(DeChainUpDater.UpDaterStatus.STATUS_FAILED);
            KozuZen.createErrorReport(new IllegalArgumentException("The installed apk path is null."));
            return START_NOT_STICKY;
        }
        startForeground(startID, NotificationProvider.buildNotification(this, NotificationProvider.NotificationIcon.NONE, R.string.notification_update_title, R.string.notification_update_desc));

        PackageInstaller installer = getPackageManager().getPackageInstaller();
        PackageInstaller.SessionParams params =
                new PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL);
        try {
            int sessionId = installer.createSession(params);
            try(PackageInstaller.Session session = installer.openSession(sessionId);
                OutputStream out = session.openWrite("latest.apk", 0, -1);
                InputStream in = new FileInputStream(installedAPKPath)) {
                nowSession = session;
                byte[] buffer = new byte[8192];
                int len;
                while((len = in.read(buffer)) != -1) out.write(buffer, 0, len);
                session.fsync(out);
            } catch(Exception e) {
                exit(DeChainUpDater.UpDaterStatus.STATUS_FAILED);
                KozuZen.createErrorReport(e);
                return START_NOT_STICKY;
            }

            Intent doneIntent = new Intent(KozuZen.getInstance(), DownloadExitReceiver.class);
            doneIntent.putExtra(Constants.IntentExtraKey.RECEIVER_EXIT_CODE, DeChainUpDater.UpDaterStatus.STATUS_SUCCESS.getID());
            doneIntent.putExtra(Constants.IntentExtraKey.RECEIVER_EXIT_SESSION_ID, sessionId);
            doneIntent.putExtra(Constants.IntentExtraKey.RECEIVER_EXIT_APK_PATH, installedAPKPath);
            sendBroadcast(doneIntent);
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                stopForeground(true);
            }, TimeUnit.SECONDS.toMillis(2));
        } catch(IOException e) {
            exit(DeChainUpDater.UpDaterStatus.STATUS_FAILED);
            KozuZen.createErrorReport(e);
            return START_NOT_STICKY;
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(nowSession != null) {
            nowSession.close();
        }
        exit(DeChainUpDater.UpDaterStatus.STATUS_STOPPING);
    }



    private void exit(DeChainUpDater.UpDaterStatus status) {
        Intent intent = new Intent(this, DownloadExitReceiver.class);
        intent.putExtra(Constants.IntentExtraKey.RECEIVER_EXIT_CODE, status.getID());
        sendBroadcast(intent);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            stopForeground(true);
        }, TimeUnit.SECONDS.toMillis(2));
    }
}