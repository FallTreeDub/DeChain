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
 * <p>アカウント仮登録が済んでいる状態でアプリを起動する際、優先的にこの内部アカウントのメールアドレス、パスワードでログインする。</p>
 */
public final class InternalTentativeAccount implements InternalAccount {

    @NotNull
    private final String mailAddress;
    @NotNull
    private final HashedString encryptedPassword;

    private static final String KEY_MAIL_ADDRESS = "accountMailAddress";
    private static final String KEY_PASSWORD = "accountPassword";

    /**
     * {@code InternalTentativeAccount}のコンストラクタ。
     * このオブジェクトはアカウントの読み出しのみに使うものとする。
     * @param mailAddress アカウントのメールアドレス。
     * @param encryptedPassword SHA-256にて暗号化されたパスワード。
     */
    private InternalTentativeAccount(@NotNull String mailAddress, @NotNull HashedString encryptedPassword) {
        this.mailAddress = mailAddress;
        this.encryptedPassword = encryptedPassword;
    }

    @Override
    @NotNull
    public String getMailAddress() {
        return this.mailAddress;
    }

    @Override
    @NotNull
    public HashedString getEncryptedPassword() {
        return this.encryptedPassword;
    }

    /**
     * アプリ内ストレージにログイン済みとしてjsonファイルにアカウントを登録する。
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
     * ログイン済みとしてアプリ内ストレージに登録された内部アカウントを取得する。
     * あくまでアプリ内に登録されたアカウントを取得するので、それがSpreadsheet上に存在するかどうかは別途判定が必要である。
     * @return ログイン済みとして登録された内部アカウント。存在しない場合はnullが返される。
     */
    @Nullable
    public static InternalTentativeAccount get() {
        SharedPreferences pref = KozuZen.getInstance().getSharedPreferences(Constants.SharedPreferences.PATH_TENTATIVE_REGISTER_STATUS, Context.MODE_PRIVATE);
        String mail = pref.getString(KEY_MAIL_ADDRESS, "");
        HashedString pass = HashedString.as(pref.getString(KEY_PASSWORD, ""));
        if(mail.isEmpty() || pass == null) return null;
        return new InternalTentativeAccount(mail, pass);
    }

    /**
     * 登録されている内部アカウントを削除する。
     */
    public static void remove() {
        SharedPreferences pref = KozuZen.getInstance().getSharedPreferences(Constants.SharedPreferences.PATH_TENTATIVE_REGISTER_STATUS, Context.MODE_PRIVATE);
        pref.edit().remove(KEY_MAIL_ADDRESS).apply();
        pref.edit().remove(KEY_PASSWORD).apply();
    }
}
