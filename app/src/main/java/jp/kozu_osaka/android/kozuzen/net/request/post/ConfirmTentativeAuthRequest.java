package jp.kozu_osaka.android.kozuzen.net.request.post;

import jp.kozu_osaka.android.kozuzen.net.argument.post.ConfirmTentativeAuthArguments;

public final class ConfirmTentativeAuthRequest extends PostRequest {

    public static final int ERROR_CODE_INCORRECT = 2001;

    public ConfirmTentativeAuthRequest(ConfirmTentativeAuthArguments args) {
        super(RequestType.CONFIRM_TENTATIVE_AUTHCODE, args);
    }
}
