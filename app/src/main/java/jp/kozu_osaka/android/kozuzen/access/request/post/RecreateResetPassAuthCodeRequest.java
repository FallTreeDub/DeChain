package jp.kozu_osaka.android.kozuzen.access.request.post;

import jp.kozu_osaka.android.kozuzen.access.argument.Arguments;
import jp.kozu_osaka.android.kozuzen.access.argument.post.RecreateResetPassAuthCodeArguments;

public final class RecreateResetPassAuthCodeRequest extends PostRequest {

    public RecreateResetPassAuthCodeRequest(RecreateResetPassAuthCodeArguments args) {
        super(RequestType.RECREATE_RESET_PASS_AUTHCODE, args);
    }
}
