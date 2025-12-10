package jp.kozu_osaka.android.kozuzen.net.update;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInstaller;
import android.net.Uri;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import jp.kozu_osaka.android.kozuzen.Constants;
import jp.kozu_osaka.android.kozuzen.KozuZen;

/**
 * DeChainのアップデートを担当する。
 */
public final class DeChainUpDater {

    public static final String SHARED_PREFERENCE_KEY_STATUS = "nowStatus";
    public static final String SHARED_PREFERENCE_KEY_INSTALLED_APK_PATH = "installedAPKPath";
    public static final String SHARED_PREFERENCE_KEY_INSTALLING_SESSION_ID = "installingSessionID";

    private DeChainUpDater() {}

    /**
     * 非同期的にアップデートを行う。
     * @param context
     */
    public static void enqueueUpdate(Context context) throws IllegalStateException {
        if(isRunning(context)) throw new IllegalStateException("DeChain Updater is running.");
        setStatus(context, UpDaterStatus.STATUS_RUNNING);
        Intent downloadIntent = new Intent(context, DownloadService.class);
        context.startService(downloadIntent);
    }

    public static boolean isRunning(Context context) {
        return getStatus(context) == UpDaterStatus.STATUS_RUNNING;
    }

    public static void setStatus(Context context, UpDaterStatus status) {
        SharedPreferences pref = context.getSharedPreferences(Constants.SharedPreferences.PATH_UPDATE_PROCESS_STATUS, Context.MODE_PRIVATE);
        pref.edit().putInt(SHARED_PREFERENCE_KEY_STATUS, status.getID()).apply();
    }

    /**
     * ダウンロード成功時にアプリがバックグラウンドである場合、次にアプリを開いたときにアップデートをすぐにインストールできるように準備する。
     */
    static void saveInstallingInfo(Context context, int sessionID, String installedAPKPath) {
        SharedPreferences pref = context.getSharedPreferences(Constants.SharedPreferences.PATH_UPDATE_PROCESS_STATUS, Context.MODE_PRIVATE);
        pref.edit()
                .putString(SHARED_PREFERENCE_KEY_INSTALLED_APK_PATH, installedAPKPath)
                .putInt(SHARED_PREFERENCE_KEY_INSTALLING_SESSION_ID, sessionID)
                .apply();
    }

    public static void removeInstallingInfo(Context context) {
        SharedPreferences pref = context.getSharedPreferences(Constants.SharedPreferences.PATH_UPDATE_PROCESS_STATUS, Context.MODE_PRIVATE);
        pref.edit().clear().apply();
    }

    public static String getInstalledAPKPath(Context context) {
        SharedPreferences pref = context.getSharedPreferences(Constants.SharedPreferences.PATH_UPDATE_PROCESS_STATUS, Context.MODE_PRIVATE);
        return pref.getString(SHARED_PREFERENCE_KEY_INSTALLED_APK_PATH, null);
    }

    public static int getInstallingSessionID(Context context) {
        SharedPreferences pref = context.getSharedPreferences(Constants.SharedPreferences.PATH_UPDATE_PROCESS_STATUS, Context.MODE_PRIVATE);
        return pref.getInt(SHARED_PREFERENCE_KEY_INSTALLING_SESSION_ID, -1);
    }

    public static UpDaterStatus getStatus(Context context) {
        SharedPreferences pref = context.getSharedPreferences(Constants.SharedPreferences.PATH_UPDATE_PROCESS_STATUS, Context.MODE_PRIVATE);
        if(!pref.contains(SHARED_PREFERENCE_KEY_STATUS)) {
            setStatus(context, UpDaterStatus.STATUS_STOPPING);
        }
        return UpDaterStatus.from(pref.getInt(SHARED_PREFERENCE_KEY_STATUS, 0));
    }

    /**
     * フォアグラウンドの画面上にアップデートの承認を求めるダイアログを表示する。
     * @param targetAPKFile
     * @param sessionID
     * @throws FileNotFoundException
     */
    public static void showUpdateRequestDialog(Context context, File targetAPKFile, int sessionID) throws FileNotFoundException, SecurityException {
        if(!targetAPKFile.exists()) throw new FileNotFoundException("target APK file does not exist.");
        if(KozuZen.getCurrentActivity() == null) throw new IllegalStateException("App is not foreground.");

        PackageInstaller installer = context.getPackageManager().getPackageInstaller();
        try(PackageInstaller.Session session = installer.openSession(sessionID)) {
            Intent updateDialogIntent = new Intent(context, UpdateDialogBroadcastReceiver.class);
            updateDialogIntent.putExtra(Constants.IntentExtraKey.RECEIVER_EXIT_APK_PATH, targetAPKFile.getAbsolutePath());
            PendingIntent pi = PendingIntent.getBroadcast(
                    context, 0, updateDialogIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE
            );
            session.commit(pi.getIntentSender());
        } catch(IOException e) {
            KozuZen.createErrorReport(e);
        }
    }

    public static final class UpdateDialogBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String installedAPKFilePath = intent.getStringExtra(Constants.IntentExtraKey.RECEIVER_EXIT_APK_PATH);
            if(installedAPKFilePath == null) {
                KozuZen.createErrorReport(new IllegalArgumentException("Installed apk file path thrown to UpdateDialogBroadcastReceiver is null."));
                return;
            }
            Uri contentUri = FileProvider.getUriForFile(
                    context,
                    KozuZen.getInstance().getPackageName() + ".provider",
                    new File(installedAPKFilePath)
            );
            Intent dialogIntent = new Intent(Intent.ACTION_VIEW);
            dialogIntent.setDataAndType(
                    contentUri,
                    "application/vnd.android.package-archive"
            );
            dialogIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            dialogIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(dialogIntent);
        }
    }
    
    public enum UpDaterStatus {
        /**
         * アップデート準備作業が終わり、ユーザーによって完全にインストールされた状態。
         */
        STATUS_STOPPING(0),
        STATUS_RUNNING(1),
        STATUS_SUCCESS(2),
        /**
         * エラーが出た際の強制終了時。
         */
        STATUS_FAILED(3);
        
        private final int ID;
        
        UpDaterStatus(int id) {
            this.ID = id;
        }
        
        public int getID() {
            return this.ID;
        }
        
        public static boolean isValid(int id) {
            for(UpDaterStatus s : values()) {
                if(s.getID() == id) return true;
            }
            return false;
        }

        public static UpDaterStatus from(int id) {
            for(UpDaterStatus s : values()) {
                if(s.getID() == id) return s;
            }
            return STATUS_STOPPING;
        }
    }
}
