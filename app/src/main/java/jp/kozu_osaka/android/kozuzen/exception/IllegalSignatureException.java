package jp.kozu_osaka.android.kozuzen.exception;

/**
 * このアプリの署名が不正な場合にスローされる。
 * サービスアカウントをWeb上から取得するときのアプリ署名確認時にスローされることがある。
 */
public final class IllegalSignatureException extends Exception {
    public IllegalSignatureException(String msg) {
        super(msg);
    }
}
