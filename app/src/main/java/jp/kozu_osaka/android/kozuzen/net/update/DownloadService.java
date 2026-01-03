package jp.kozu_osaka.android.kozuzen.net.update;

import android.app.DownloadManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.concurrent.TimeUnit;

import jp.kozu_osaka.android.kozuzen.Constants;
import jp.kozu_osaka.android.kozuzen.KozuZen;
import jp.kozu_osaka.android.kozuzen.R;
import jp.kozu_osaka.android.kozuzen.exception.GetAccessException;
import jp.kozu_osaka.android.kozuzen.net.DataBaseAccessor;
import jp.kozu_osaka.android.kozuzen.net.DataBaseGetResponse;
import jp.kozu_osaka.android.kozuzen.net.argument.get.GetLatestVersionApkLinkArguments;
import jp.kozu_osaka.android.kozuzen.net.callback.GetAccessCallBack;
import jp.kozu_osaka.android.kozuzen.net.request.Request;
import jp.kozu_osaka.android.kozuzen.net.request.get.GetLatestVersionApkLinkRequest;
import jp.kozu_osaka.android.kozuzen.util.NotificationProvider;

/**
 * <p>
 *     {@link GetLatestVersionApkLinkRequest}でデータベースから最新のバージョンのDeChainアプリのGoogle Driveリンクを取得し、
 *     ファイルをダウンロードするService。
 *     Google Drive上のファイルは、Googleのウイルススキャンを回避するためZIP形式である。
 * </p>
 * <p>
 *     アップデートの詳しい処理の流れは{@code package-info.java}のjavadocへ。
 * </p>
 *
 * @see DeChainUpDater
 */
public final class DownloadService extends Service {

    private File installedApkFile = null;
    private File installedZipFile = null;
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
                exit(DeChainUpDater.UpDaterStatus.STATUS_FAILED);
                KozuZen.createErrorReport(new IllegalArgumentException("download id from intent extra is not the same to one of DownloadService 'downloadID'."));
                return;
            }
            installedZipFile.renameTo(installedApkFile); //zipファイルをapkにリネーム
            Intent installServiceIntent = new Intent(context, InstallService.class);
            installServiceIntent.putExtra(Constants.IntentExtraKey.UPDATE_INSTALLED_APK_PATH, installedApkFile.getAbsolutePath());
            context.startService(installServiceIntent);
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        installedApkFile = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "latest.apk");
        installedZipFile = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "latest.zip");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startID) {
        if(installedApkFile == null || installedZipFile == null) {
            exit(DeChainUpDater.UpDaterStatus.STATUS_FAILED);
            KozuZen.createErrorReport(new IllegalStateException("installedAPKFile or installedZip file is null.(APK:" + installedApkFile + ", ZIP:" + installedZipFile));
            return START_NOT_STICKY;
        }
        startForeground(startID, NotificationProvider.buildNotification(this, NotificationProvider.NotificationIcon.NONE, R.string.notification_update_title, R.string.notification_update_desc));
        registerReceiver(downloadDoneReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), RECEIVER_EXPORTED);

        if(installedApkFile.exists()) { //すでにアップデートをしたことがあるなら古いAPKファイルを削除
            installedApkFile.delete();
        }

        //リンクをデータベースから取得
        GetLatestVersionApkLinkRequest req = new GetLatestVersionApkLinkRequest(new GetLatestVersionApkLinkArguments());
        GetAccessCallBack<String> callBack = new GetAccessCallBack<>(req) {
            @Override
            public void onSuccess(@NotNull DataBaseGetResponse response) {
                String responseResult = this.getRequest.resultParse(response.getResultJsonElement());
                final Uri apkZIPUri;
                try {
                    if(responseResult.isEmpty()) throw new GetAccessException(R.string.error_database_update_apkLinkIsEmpty);
                    apkZIPUri = Uri.parse(responseResult);
                } catch(NullPointerException | GetAccessException e) {
                    exit(DeChainUpDater.UpDaterStatus.STATUS_FAILED);
                    KozuZen.createErrorReport(e);
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
            public void onFailure(@Nullable DataBaseGetResponse response) {
                exit(DeChainUpDater.UpDaterStatus.STATUS_FAILED);

                @StringRes Integer msgID = null;
                if(response != null) {
                    switch(response.getResponseCode()) {
                        case Request.RESPONSE_CODE_ARGUMENT_NULL:
                            msgID = R.string.error_argNull;
                            break;
                        case Request.RESPONSE_CODE_ARGUMENT_NON_SIGNATURES:
                            msgID = R.string.error_notFoundSignatures;
                            break;
                    }
                }
                if(msgID == null) msgID = R.string.error_unknown;

                KozuZen.createErrorReport(new GetAccessException(msgID));
            }

            @Override
            public void onTimeOut() {
                NotificationProvider.sendNotification(
                        NotificationProvider.NotificationTitle.UPDATE_FAILED,
                        NotificationProvider.NotificationIcon.NONE,
                        R.string.notification_update_desc_fail_connection
                );
                exit(DeChainUpDater.UpDaterStatus.STATUS_FAILED);
            }
        };
        DataBaseAccessor.sendGetRequest(req, callBack);
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        DownloadManager dm = (DownloadManager)getSystemService(Context.DOWNLOAD_SERVICE);
        if(downloadID != -1) {
            dm.remove(downloadID);
        }
        exit(DeChainUpDater.UpDaterStatus.STATUS_STOPPING);
    }

    private void exit(DeChainUpDater.UpDaterStatus status) {
        Intent errorIntent = new Intent(this, DownloadExitReceiver.class);
        errorIntent.putExtra(Constants.IntentExtraKey.RECEIVER_EXIT_CODE, status.getID());
        sendBroadcast(errorIntent);
        new Handler(Looper.getMainLooper()).postDelayed(() -> { //すぐに止めるとsendBroadcastの前にservice落ちる
            stopForeground(true);
        }, TimeUnit.SECONDS.toMillis(2));
    }
}