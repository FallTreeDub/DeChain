package jp.kozu_osaka.android.kozuzen.net.request.post;

import jp.kozu_osaka.android.kozuzen.net.argument.post.TentativeRegisterArguments;

public final class TentativeRegisterRequest extends PostRequest {

    public TentativeRegisterRequest(TentativeRegisterArguments args) {
        super(RequestType.REGISTER_TENTATIVE, args);
    }
}
