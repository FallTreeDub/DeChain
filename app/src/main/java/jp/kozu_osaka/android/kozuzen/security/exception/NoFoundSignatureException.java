package jp.kozu_osaka.android.kozuzen.security.exception;

/**
 * このアプリの署名が存在しない場合にスローされる。
 * サービスアカウントをWeb上から取得するときのアプリ署名確認時にスローされることがある。
 */
public final class NoFoundSignatureException extends Exception {
    public NoFoundSignatureException(String message) {
        super(message);
    }
}
