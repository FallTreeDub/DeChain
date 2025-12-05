package jp.kozu_osaka.android.kozuzen.internal;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Calendar;

import jp.kozu_osaka.android.kozuzen.Constants;
import jp.kozu_osaka.android.kozuzen.ExperimentType;
import jp.kozu_osaka.android.kozuzen.KozuZen;
import jp.kozu_osaka.android.kozuzen.util.PermissionsStatus;
import jp.kozu_osaka.android.kozuzen.net.usage.UsageDataBroadcastReceiver;
import jp.kozu_osaka.android.kozuzen.security.HashedString;
import jp.kozu_osaka.android.kozuzen.util.Logger;

/**
 * <p>SharedPreferencesに保管される内部本登録アカウント。</p>
 * <p>アカウント本登録が済んでいる状態でアプリを起動する際、優先的にこの内部アカウントのメールアドレス、パスワードでログインする。</p>
 */
public final class InternalRegisteredAccountManager {

    private static final String KEY_MAIL_ADDRESS = "accountMailAddress";
    private static final String KEY_PASSWORD = "accountPassword";
    private static final String KEY_EXPERIMENT_TYPE = "experimentType";

    private InternalRegisteredAccountManager() {}

    public static String getMailAddress() {
        SharedPreferences pref = KozuZen.getInstance().getSharedPreferences(Constants.SharedPreferences.PATH_LOGIN_STATUS, Context.MODE_PRIVATE);
        return pref.getString(KEY_MAIL_ADDRESS, null);
    }

    public static HashedString getEncryptedPassword() {
        SharedPreferences pref = KozuZen.getInstance().getSharedPreferences(Constants.SharedPreferences.PATH_LOGIN_STATUS, Context.MODE_PRIVATE);
        return HashedString.as(pref.getString(KEY_PASSWORD, null));
    }

    public static ExperimentType getExperimentType() {
        SharedPreferences pref = KozuZen.getInstance().getSharedPreferences(Constants.SharedPreferences.PATH_LOGIN_STATUS, Context.MODE_PRIVATE);
        return ExperimentType.getFromID(pref.getInt(KEY_EXPERIMENT_TYPE, -1));
    }

    /**
     * アプリ内ストレージにログイン済みとしてSharedPreferencesに登録する。
     */
    @SuppressLint("ScheduleExactAlarm")
    public static void register(@NotNull Context context, @NotNull String mail, @NotNull HashedString encryptedPassword, @NotNull ExperimentType experimentType) {

        SharedPreferences pref = KozuZen.getInstance().getSharedPreferences(Constants.SharedPreferences.PATH_LOGIN_STATUS, Context.MODE_PRIVATE);
        pref.edit()
                .putString(KEY_MAIL_ADDRESS, mail)
                .putString(KEY_PASSWORD, encryptedPassword.toString())
                .putInt(KEY_EXPERIMENT_TYPE, experimentType.getID())
                .apply();

        //一日ごとの通知送信タスクをAlarmManagerでpending
        //通知送信時間を現在時刻基準での次の20時に設定
        UsageDataBroadcastReceiver.pendThis(context);

        Logger.i("Internal account is registered.");
    }

    public static boolean isRegistered() {
        return getMailAddress() != null;
    }

    public static void changePassword(@NotNull HashedString newPassword) {
        SharedPreferences pref = KozuZen.getInstance().getSharedPreferences(Constants.SharedPreferences.PATH_LOGIN_STATUS, Context.MODE_PRIVATE);
        pref.edit()
                .putString(KEY_PASSWORD, newPassword.toString())
                .apply();
        Logger.i("A new password has been registered in the reged account.");
    }

    /**
     * 登録されている内部アカウントを削除する。
     */
    public static void remove(Context context) {
        SharedPreferences pref = KozuZen.getInstance().getSharedPreferences(Constants.SharedPreferences.PATH_LOGIN_STATUS, Context.MODE_PRIVATE);
        pref.edit().remove(KEY_MAIL_ADDRESS).apply();
        pref.edit().remove(KEY_PASSWORD).apply();
        pref.edit().remove(KEY_EXPERIMENT_TYPE).apply();

        //pendingされたbroadcastReceiverキャンセル
        UsageDataBroadcastReceiver.cancelThis(context);
    }
}
