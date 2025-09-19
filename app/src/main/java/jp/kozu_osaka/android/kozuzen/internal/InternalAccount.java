package jp.kozu_osaka.android.kozuzen.internal;

import jp.kozu_osaka.android.kozuzen.security.HashedString;

/**
 * Androidデバイス内部のSharedPreferencesに格納する
 * アカウント情報のインターフェース。メールアドレスとパスワードを最小要素とする。
 */
public interface InternalAccount extends Internal {

    /**
     * オブジェクトに紐づけられたメールアドレスを返す。
     * @return メールアドレス。
     */
    String getMailAddress();

    /**
     * SHA-256によって暗号化された文字列である{@link HashedString}。
     * オブジェクトに対するパスワードとして扱う。
     * @return HashedString。
     */
    HashedString getEncryptedPassword();
}
