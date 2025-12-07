package jp.kozu_osaka.android.kozuzen.net.request.post;

import jp.kozu_osaka.android.kozuzen.net.argument.post.TentativeRegisterArguments;

public final class TentativeRegisterRequest extends PostRequest {

    public static final int ERROR_CODE_INTERNAL = 1001;
    public static final int ERROR_CODE_AFTER_START = 1002;

    public TentativeRegisterRequest(TentativeRegisterArguments args) {
        super(RequestType.REGISTER_TENTATIVE, args);
    }
}
