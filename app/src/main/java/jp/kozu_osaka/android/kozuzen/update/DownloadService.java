package jp.kozu_osaka.android.kozuzen.update;

import android.app.DownloadManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInstaller;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;

import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import jp.kozu_osaka.android.kozuzen.Constants;
import jp.kozu_osaka.android.kozuzen.KozuZen;
import jp.kozu_osaka.android.kozuzen.R;
import jp.kozu_osaka.android.kozuzen.access.DataBaseAccessor;
import jp.kozu_osaka.android.kozuzen.access.argument.get.GetLatestVersionApkLinkArguments;
import jp.kozu_osaka.android.kozuzen.access.callback.GetAccessCallBack;
import jp.kozu_osaka.android.kozuzen.access.request.get.GetLatestVersionApkLinkRequest;
import jp.kozu_osaka.android.kozuzen.exception.GetAccessException;
import jp.kozu_osaka.android.kozuzen.util.NotificationProvider;

/**
 * データベースから取得したファイルリンクをもとに、ファイルをダウンロードするService。
 */
final class DownloadService extends Service {

    private final File installedApkFile = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "latest.apk");
    private final File installedZipFile = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "latest.zip");
    private long downloadID = -1;

    /**
     * ダウンロード処理が完了した後。InstallServiceに引き継ぐ
     */
    private final BroadcastReceiver downloadDoneReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            context.unregisterReceiver(this);

            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            if(id != downloadID) {
                sendExitReceiver(DeChainUpDater.STATUS_FAILED);
                KozuZen.createErrorReport(new IllegalArgumentException("download id from intent extra is not the same to one of DownloadService 'downloadID'."));
                return;
            }
            installedZipFile.renameTo(installedApkFile);
            Intent installServiceIntent = new Intent(context, InstallService.class);
            context.startService(installServiceIntent);
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startID) {
        startForeground(startID, NotificationProvider.buildNotification(this, NotificationProvider.NotificationIcon.NONE, R.string.notification_update_title, R.string.notification_update_desc));
        registerReceiver(downloadDoneReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), RECEIVER_EXPORTED);

        if(installedApkFile.exists()) { //すでにアップデートをしたことがあるなら古いAPKファイルを削除
            installedApkFile.delete();
        }

        //リンクをデータベースから取得
        GetLatestVersionApkLinkRequest req = new GetLatestVersionApkLinkRequest(new GetLatestVersionApkLinkArguments());
        GetAccessCallBack<String> callBack = new GetAccessCallBack<>(req) {
            @Override
            public void onSuccess(@NotNull String responseResult) {
                final Uri apkZIPUri;
                try {
                    apkZIPUri = Uri.parse(responseResult);
                } catch(NullPointerException e) {
                    sendExitReceiver(DeChainUpDater.STATUS_FAILED);
                    KozuZen.createErrorReport(e);
                    stopForeground(true);
                    return;
                }
                DownloadManager dm = (DownloadManager)getSystemService(Context.DOWNLOAD_SERVICE);
                DownloadManager.Request downloadReq = new DownloadManager.Request(apkZIPUri);
                downloadReq.setTitle(getString(R.string.notification_update_title));
                downloadReq.setDescription(getString(R.string.notification_update_desc));
                downloadReq.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
                downloadReq.setDestinationInExternalFilesDir(DownloadService.this, Environment.DIRECTORY_DOWNLOADS, installedZipFile.getName());

                downloadID = dm.enqueue(downloadReq);
            }

            @Override
            public void onFailure(int responseCode, String message) {
                sendExitReceiver(DeChainUpDater.STATUS_FAILED);
                KozuZen.createErrorReport(new GetAccessException(responseCode, message));
                stopForeground(true);
            }

            @Override
            public void onTimeOut() {
                retry();
                NotificationProvider.sendNotification(
                        NotificationProvider.NotificationTitle.UPDATE_FAILED,
                        NotificationProvider.NotificationIcon.NONE,
                        R.string.notification_update_desc_fail_connection
                );
                sendExitReceiver(DeChainUpDater.STATUS_FAILED);
                stopForeground(true);
            }
        };
        DataBaseAccessor.sendGetRequest(req, callBack);

        try {
            installPackage();
        } catch(IOException e) {
            sendExitReceiver(DeChainUpDater.STATUS_FAILED);
            KozuZen.createErrorReport(e);
            stopForeground(true);
        }

        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent intent) {
        super.onTaskRemoved(intent);
        sendExitReceiver(DeChainUpDater.STATUS_STOPPING);
        stopForeground(true);
    }

    private void sendExitReceiver(int status) {
        Intent errorIntent = new Intent(Constants.IntentAction.UPDATE_EXIT);
        errorIntent.putExtra(Constants.IntentExtraKey.RECEIVER_EXIT_CODE, status);
        sendBroadcast(errorIntent);
    }

    private void installPackage() throws IOException, SecurityException {
        PackageInstaller installer = getPackageManager().getPackageInstaller();
        PackageInstaller.SessionParams params =
                new PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL);
        int sessionId = installer.createSession(params);
        try(PackageInstaller.Session session = installer.openSession(sessionId);
            OutputStream out = session.openWrite("test.apk", 0, -1);
            InputStream in = new FileInputStream(installedApkFile)) {

            byte[] buffer = new byte[8192];
            int len;
            while ((len = in.read(buffer)) != -1) out.write(buffer, 0, len);
            session.fsync(out);
        }//SecurityException, IOExceptionは上に投げる


    }
}