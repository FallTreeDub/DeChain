package jp.kozu_osaka.android.kozuzen.security;

import android.util.Base64;

import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * ハッシュ関数(SHA-256)によって暗号化された文字列のBase64の実体。
 */
public final class HashedString implements Serializable {

    private final String hashedString;

    private HashedString(String encrypted) {
        this.hashedString = encrypted;
    }

    /**
     * すでにSHA-256でハッシュ値化しているBase64文字列{@code encrypted}を用いて
     * {@code HashedString}のインスタンスを作成する。{@code encrypted}がnullまたは空文字列の場合はnullを返す。
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
     * @throws NoSuchAlgorithmException SHA-256が見つからない場合。
     */
    public static HashedString encrypt(String plain) throws NoSuchAlgorithmException {
        if(plain == null || plain.isEmpty()) return null;

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] encryptedByte = md.digest(plain.getBytes()); //SHA-256の暗号化結果をbyte[]として格納
        String encryptedStr = Base64.encodeToString(encryptedByte, Base64.DEFAULT);
        return HashedString.as(encryptedStr);
    }

    /**
     * 暗号化した結果としてのBase64文字列を返す。
     */
    @Override
    @NotNull
    public String toString() {
        return this.hashedString;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof HashedString)) return false;
        return obj.toString().equals(this.toString());
    }
}
