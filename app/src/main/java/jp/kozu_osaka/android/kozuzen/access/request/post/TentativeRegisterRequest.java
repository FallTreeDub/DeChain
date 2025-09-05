package jp.kozu_osaka.android.kozuzen.access.request.post;

import jp.kozu_osaka.android.kozuzen.access.argument.post.TentativeRegisterArguments;

public final class TentativeRegisterRequest extends PostRequest {

    public TentativeRegisterRequest(TentativeRegisterArguments args) {
        super(RequestType.REGISTER_TENTATIVE, args);
    }
}
