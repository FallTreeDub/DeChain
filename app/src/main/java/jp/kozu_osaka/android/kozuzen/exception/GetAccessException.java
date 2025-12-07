package jp.kozu_osaka.android.kozuzen.exception;

import android.content.Context;

import androidx.annotation.StringRes;

import org.jetbrains.annotations.NotNull;

import jp.kozu_osaka.android.kozuzen.KozuZen;
import jp.kozu_osaka.android.kozuzen.net.DataBaseGetResponse;
import jp.kozu_osaka.android.kozuzen.net.callback.GetAccessCallBack;

/**
 * データベースから処理失敗のレスポンスコードが返されるか、接続自体が失敗、もしくはデータベースとの接続が成功しても
 * 返り値が無効である場合にスローされる。ユーザーにこのエラーを報告させるために、try-catchのcatch部では
 * {@link jp.kozu_osaka.android.kozuzen.KozuZen#createErrorReport(Context, Exception)}もしくは
 * {@link jp.kozu_osaka.android.kozuzen.KozuZen#createErrorReport(Exception)}で報告画面を出す必要がある。
 */
public final class GetAccessException extends Exception {

    public GetAccessException(@StringRes int databaseErrorOrUserErrorResID) {
        super(KozuZen.getInstance().getString(databaseErrorOrUserErrorResID));
    }

    public GetAccessException(@NotNull DataBaseGetResponse errorResponse) {
        super(errorResponse.getResponseCode() + ", " +
                (errorResponse.getMessage() == null ? "" : errorResponse.getMessage()));
    }
}
