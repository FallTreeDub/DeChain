package jp.kozu_osaka.android.kozuzen.access.request.post;

import jp.kozu_osaka.android.kozuzen.access.argument.post.ResetPasswordArguments;

/**
 * 本登録済みアカウントのパスワードを新しいものにリセットするように
 * データベースにリクエストする。アカウントのメールアドレスがデータベースに登録されていない場合は
 * HTTP response 400番レスポンスが返され、リセットすることはできない。
 */
public final class ResetPasswordRequest extends PostRequest {

    public ResetPasswordRequest(ResetPasswordArguments args) {
        super(RequestType.REQUEST_RESET_PASS, args);
    }
}
