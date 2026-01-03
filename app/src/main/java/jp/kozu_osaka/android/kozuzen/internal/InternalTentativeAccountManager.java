package jp.kozu_osaka.android.kozuzen.internal;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import jp.kozu_osaka.android.kozuzen.Constants;
import jp.kozu_osaka.android.kozuzen.KozuZen;
import jp.kozu_osaka.android.kozuzen.security.HashedString;
import jp.kozu_osaka.android.kozuzen.util.Logger;

/**
 * <p>SharedPreferencesに保管される内部仮登録アカウント。</p>
 * <p>アカウント仮登録が済んでいる状態でアプリを起動する際、優先的にこの内部アカウントのメールアドレス、パスワードで6桁認証画面へ進む。</p>
 */
public final class InternalTentativeAccountManager {

    private static final String KEY_MAIL_ADDRESS = "accountMailAddress";
    private static final String KEY_PASSWORD = "accountPassword";

    public static String getMailAddress() {
        SharedPreferences pref = KozuZen.getInstance().getSharedPreferences(Constants.SharedPreferences.PATH_TENTATIVE_REGISTER_STATUS, Context.MODE_PRIVATE);
        return pref.getString(KEY_MAIL_ADDRESS, null);
    }

    public static HashedString getEncryptedPassword() {
        SharedPreferences pref = KozuZen.getInstance().getSharedPreferences(Constants.SharedPreferences.PATH_TENTATIVE_REGISTER_STATUS, Context.MODE_PRIVATE);
        return HashedString.as(pref.getString(KEY_PASSWORD, null));
    }

    /**
     * アプリ内ストレージにログイン済みとしてSharedPreferencesにアカウントを登録する。
     */
    public static void register(String mail, HashedString encryptedPassword) {
        SharedPreferences pref = KozuZen.getInstance().getSharedPreferences(Constants.SharedPreferences.PATH_TENTATIVE_REGISTER_STATUS, Context.MODE_PRIVATE);
        pref.edit()
                .putString(KEY_MAIL_ADDRESS, mail)
                .putString(KEY_PASSWORD, encryptedPassword.toString())
                .apply();

        Logger.i("Internal account is registered.");
    }

    /**
     * すでに仮登録アカウントが存在しているか。
     */
    public static boolean isRegistered() {
        return getMailAddress() != null;
    }

    /**
     * 登録されている内部アカウントを削除する。
     */
    public static void remove() {
        SharedPreferences pref = KozuZen.getInstance().getApplicationContext().getSharedPreferences(Constants.SharedPreferences.PATH_TENTATIVE_REGISTER_STATUS, Context.MODE_PRIVATE);
        pref.edit().clear().apply();
    }
}
