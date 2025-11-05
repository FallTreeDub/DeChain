package jp.kozu_osaka.android.kozuzen.exception;

import android.content.Context;

import jp.kozu_osaka.android.kozuzen.access.DataBasePostResponse;

/**
 * HTTP POSTアクセスに失敗した際に{@link jp.kozu_osaka.android.kozuzen.KozuZen#createErrorReport(Context, Exception)}で
 * ユーザーに報告画面を表示するために使用する。
 */
public final class PostAccessException extends Exception {

    public PostAccessException(DataBasePostResponse response) {
        super(response.getResponseCode() + ", " + response.getResponseMessage());
    }

    public PostAccessException(DataBasePostResponse response, String msg) {
        super(response.getResponseCode() + ", " + response.getResponseMessage() + " : " + msg);
    }
}
