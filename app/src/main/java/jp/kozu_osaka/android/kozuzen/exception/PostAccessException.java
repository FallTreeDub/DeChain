package jp.kozu_osaka.android.kozuzen.exception;

import jp.kozu_osaka.android.kozuzen.access.DataBasePostResponse;

/**
 * HTTP POSTアクセスに失敗したときに投げられる。
 */
public class PostAccessException extends Exception {

    public PostAccessException(DataBasePostResponse response) {
        super(response.getResponseCode() + ", " + response.getResponseMessage());
    }

    public PostAccessException(DataBasePostResponse response, String msg) {
        super(response.getResponseCode() + ", " + response.getResponseMessage() + " : " + msg);
    }
}
