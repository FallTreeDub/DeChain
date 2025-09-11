package jp.kozu_osaka.android.kozuzen.access.request.post;

import jp.kozu_osaka.android.kozuzen.access.argument.Arguments;
import jp.kozu_osaka.android.kozuzen.access.argument.post.RecreateTentativeAuthCodeArguments;
import jp.kozu_osaka.android.kozuzen.access.request.Request;

public final class RecreateTentativeAuthCodeRequest extends PostRequest {

    public RecreateTentativeAuthCodeRequest(RecreateTentativeAuthCodeArguments args) {
        super(Request.RequestType.RECREATE_TENTATIVE_AUTHCODE, args);
    }
}
