package jp.kozu_osaka.android.kozuzen.access.request.post;

import jp.kozu_osaka.android.kozuzen.access.argument.post.ResetPasswordArguments;

public final class ResetPasswordRequest extends PostRequest {

    public ResetPasswordRequest(ResetPasswordArguments args) {
        super(RequestType.REQUEST_RESET_PASS, args);
    }
}
