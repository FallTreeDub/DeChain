package jp.kozu_osaka.android.kozuzen.internal;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.gson.Gson;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.List;

import jp.kozu_osaka.android.kozuzen.Constants;
import jp.kozu_osaka.android.kozuzen.KozuZen;
import jp.kozu_osaka.android.kozuzen.security.HashedString;

/**
 * <p>アプリの内部ストレージにSharedPreferencesを実体として保管されている、内部アカウント。</p>
 * <p>アカウント本登録が済んでいる状態でアプリを起動する際、優先的にこの内部アカウントのメールアドレス、パスワードでログインする。</p>
 */
public class InternalRegisteredAccount implements InternalAccount {
    private final String mailAddress;
    private final HashedString encryptedPassword;

    private static final String KEY_MAILADDRESS = "accountMailAddress";
    private static final String KEY_PASSWORD = "accountPassword";

    /**
     * {@code InternalRegisteredAccount}のコンストラクタ。
     * このオブジェクトはアカウントの読み出しのみに使うものとする。
     * @param mailAddress アカウントのメールアドレス。
     * @param encryptedPassword SHA-256にて暗号化されたパスワード。
     */
    private InternalRegisteredAccount(String mailAddress, HashedString encryptedPassword) {
        this.mailAddress = mailAddress;
        this.encryptedPassword = encryptedPassword;
    }

    @Override
    public String getMailAddress() {
        return this.mailAddress;
    }

    @Override
    public HashedString getEncryptedPassword() {
        return this.encryptedPassword;
    }

    /**
     * アプリ内ストレージにログイン済みとしてSharedPreferencesに登録する。
     */
    public static void register(String mail, HashedString encryptedPassword) {
        SharedPreferences pref = KozuZen.getInstance().getSharedPreferences(Constants.SharedPreferences.PATH_LOGIN_STATUS, Context.MODE_PRIVATE);
        pref.edit()
                .putString(KEY_MAILADDRESS, mail)
                .putString(KEY_PASSWORD, encryptedPassword.toString())
                .apply();

        Log.i(Constants.Debug.LOGNAME_INFO, "Internal account is registered.");
    }

    /**
     * ログイン済みとしてアプリ内ストレージに登録された内部アカウントを取得する。
     * あくまでアプリ内に登録されたアカウントを取得するので、それがSpreadsheet上に存在するかどうかは別途判定が必要である。
     * @return ログイン済みとして登録された内部アカウント。存在しない場合はnullが返される。
     */
    @Nullable
    public static InternalRegisteredAccount get() {
        SharedPreferences pref = KozuZen.getInstance().getSharedPreferences(Constants.SharedPreferences.PATH_LOGIN_STATUS, Context.MODE_PRIVATE);
        String mail = pref.getString(KEY_MAILADDRESS, "");
        HashedString pass = HashedString.as(pref.getString(KEY_PASSWORD, ""));
        if(mail.isEmpty() || pass == null) return null;
        return new InternalRegisteredAccount(mail, pass);
    }

    /**
     * 登録されている内部アカウントを削除する。
     */
    public static void remove() {
        SharedPreferences pref = KozuZen.getInstance().getSharedPreferences(Constants.SharedPreferences.PATH_LOGIN_STATUS, Context.MODE_PRIVATE);
        pref.edit().remove(KEY_MAILADDRESS).apply();
        pref.edit().remove(KEY_PASSWORD).apply();
    }
}
