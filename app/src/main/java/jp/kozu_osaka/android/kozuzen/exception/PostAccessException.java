package jp.kozu_osaka.android.kozuzen.exception;

import android.content.Context;

import androidx.annotation.StringRes;

import org.jetbrains.annotations.NotNull;

import jp.kozu_osaka.android.kozuzen.KozuZen;
import jp.kozu_osaka.android.kozuzen.net.DataBaseGetResponse;
import jp.kozu_osaka.android.kozuzen.net.DataBasePostResponse;

/**
 * HTTP POSTアクセスにおいて、データベースから処理失敗のレスポンスコードが返されるか、接続自体が失敗、もしくはデータベースとの接続が成功しても
 * 返り値が無効(エラー値)である場合にスローされる。ユーザーにこのエラーを報告させるために、try-catchのcatch部では
 * {@link jp.kozu_osaka.android.kozuzen.KozuZen#createErrorReport(Exception)}で報告画面を出す必要がある。
 */
public final class PostAccessException extends Exception {

    public PostAccessException(@StringRes int databaseErrorOrUserErrorResID) {
        super(KozuZen.getInstance().getString(databaseErrorOrUserErrorResID));
    }

    public PostAccessException(@NotNull DataBasePostResponse errorResponse) {
        super(errorResponse.getResponseCode() + ", " +
                (errorResponse.getResponseMessage() == null ? "" : errorResponse.getResponseMessage()));
    }
}
