package jp.kozu_osaka.android.kozuzen.net.request.post;

import jp.kozu_osaka.android.kozuzen.net.argument.post.RecreateTentativeAuthCodeArguments;
import jp.kozu_osaka.android.kozuzen.net.request.Request;

public final class RecreateTentativeAuthCodeRequest extends PostRequest {

    public RecreateTentativeAuthCodeRequest(RecreateTentativeAuthCodeArguments args) {
        super(Request.RequestType.RECREATE_TENTATIVE_AUTHCODE, args);
    }
}
