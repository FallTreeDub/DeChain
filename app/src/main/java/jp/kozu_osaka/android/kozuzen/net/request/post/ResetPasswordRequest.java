package jp.kozu_osaka.android.kozuzen.net.request.post;

import jp.kozu_osaka.android.kozuzen.net.argument.post.ResetPasswordArguments;

/**
 * 本登録済みアカウントのパスワードを新しいものにリセットするように
 * データベースにリクエストする。アカウントのメールアドレスがデータベースに登録されていない場合は
 * HTTP response 400番レスポンスが返され、リセットすることはできない。
 */
public final class ResetPasswordRequest extends PostRequest {

    public static final int ERROR_CODE_NOT_FOUND_LINE = 4001;

    public ResetPasswordRequest(ResetPasswordArguments args) {
        super(RequestType.REQUEST_RESET_PASS, args);
    }
}
