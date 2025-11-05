package jp.kozu_osaka.android.kozuzen.exception;

import android.content.Context;

/**
 * HTTP GETアクセスに失敗した際に{@link jp.kozu_osaka.android.kozuzen.KozuZen#createErrorReport(Context, Exception)}で
 * ユーザーに報告画面を表示するために使用する。
 */
public final class GetAccessException extends Exception {
    public GetAccessException(int responseCode, String message) {
        super(responseCode + ", " + message);
    }
}
