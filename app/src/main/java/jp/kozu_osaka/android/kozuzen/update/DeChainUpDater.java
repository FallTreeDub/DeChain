package jp.kozu_osaka.android.kozuzen.update;

import android.content.Context;
import android.content.SharedPreferences;

import jp.kozu_osaka.android.kozuzen.Constants;
import jp.kozu_osaka.android.kozuzen.KozuZen;

/**
 * DeChainのアップデートを担当する。
 */
public final class DeChainUpDater {

    /**
     * アップデート準備作業が終わり、ユーザーによって完全にインストールされた状態。
     */
    public static final int STATUS_STOPPING = 0;
    public static final int STATUS_RUNNING = 1;
    public static final int STATUS_SUCCESS = 2;
    /**
     * エラーが出た際の強制終了時。
     */
    public static final int STATUS_FAILED = 3;

    private static final String INSTALLED_APK_FILE_NAME = "latest.apk";
    private static final String INSTALLED_ZIP_FILE_NAME = "latest.zip";

    private static final String SHARED_PREFERENCE_KEY_STATUS = "nowStatus";

    private DeChainUpDater() {}

    /**
     * 非同期的にアップデートを行う。
     * @param context
     */
    public static void enqueueUpdate(Context context) {
        setStatus(context, STATUS_RUNNING);
        //
        setStatus(context, STATUS_STOPPING);
    }

    /**
     * アップデート中の強制終了などの際にupdateをキャンセルする。
     */
    public static void cancelUpdate(Context context) {
        if(!isRunning(context)) throw new IllegalStateException("DeChain Updater is not running.");
        //service.stopForeground(true);
        setStatus(context, STATUS_STOPPING);
    }

    public static boolean isRunning(Context context) {
        return getStatus(context) == STATUS_RUNNING;
    }

    public static void setStatus(Context context, int status) {
        SharedPreferences pref = context.getSharedPreferences(Constants.SharedPreferences.PATH_UPDATE_PROCESS_STATUS, Context.MODE_PRIVATE);
        pref.edit().putInt(SHARED_PREFERENCE_KEY_STATUS, status).apply();
    }

    public static int getStatus(Context context) {
        SharedPreferences pref = context.getSharedPreferences(Constants.SharedPreferences.PATH_UPDATE_PROCESS_STATUS, Context.MODE_PRIVATE);
        if(!pref.contains(SHARED_PREFERENCE_KEY_STATUS)) {
            setStatus(context, STATUS_STOPPING);
        }
        return pref.getInt(SHARED_PREFERENCE_KEY_STATUS, 0);
    }
}
