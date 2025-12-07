package jp.kozu_osaka.android.kozuzen.net.request.post;

import jp.kozu_osaka.android.kozuzen.net.argument.post.RecreateResetPassAuthCodeArguments;

public final class RecreateResetPassAuthCodeRequest extends PostRequest {

    public static final int ERROR_CODE_NOT_FOUND_REQTIME_OR_REQCODE_LINE = 3001;

    public RecreateResetPassAuthCodeRequest(RecreateResetPassAuthCodeArguments args) {
        super(RequestType.RECREATE_RESET_PASS_AUTHCODE, args);
    }
}
