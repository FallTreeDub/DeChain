package jp.kozu_osaka.android.kozuzen.security;

import android.util.Base64;

import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * ハッシュ関数(SHA-256)によって暗号化されたBase64。
 */
public final class HashedString implements Serializable {

    private final String hashedString;

    private HashedString(String encrypted) {
        this.hashedString = encrypted;
    }

    /**
     * すでにSHA-256でハッシュ値化しているBase64文字列{@code encrypted}を用いて
     * {@code HashedString}のインスタンスを作成する。{@code encrypted}が{@code null}または空文字列の場合は{@code null}を返す。
     * @param encrypted SHA-256であらかじめハッシュ化している文字列。
     */
    @Nullable
    public static HashedString as(String encrypted) {
        if(encrypted == null || encrypted.isEmpty()) return null;
        return new HashedString(encrypted);
    }

    /**
     * <p>
     *     {@code plainString}をSHA-256でハッシュ値化し、{@link HashedString#toString()}によって取得できる
     *     Base64文字列としてインスタンス内に格納する。{@code plain}がnullまたは空文字列の場合はnullを返す。
     * </p>
     *
     * @see HashedString#toString()
     * @param plain 平文の文字列。
     * @throws NoSuchAlgorithmException SHA-256が有効な暗号化アルゴリズムとして見つからない場合。
     */
    public static HashedString encrypt(String plain) throws NoSuchAlgorithmException {
        if(plain == null || plain.isEmpty()) return null;

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] encryptedByte = md.digest(plain.getBytes()); //SHA-256の暗号化結果をbyte[]として格納
        String encryptedStr = Base64.encodeToString(encryptedByte, Base64.DEFAULT);
        encryptedStr = encryptedStr.replace("+", "%2B");
        return HashedString.as(encryptedStr);
    }

    /**
     * @return 暗号化した結果としてのBase64文字列。
     */
    @Override
    @NotNull
    public String toString() {
        return this.hashedString;
    }

    /**
     * @param obj 比較対象。
     * @return {@code obj}が{@link HashedString}であり、かつ暗号化の結果のBase64がこのオブジェクトと等しいかどうか。
     */
    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof HashedString)) return false;
        return obj.toString().equals(this.toString());
    }
}
