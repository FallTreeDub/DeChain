package jp.kozu_osaka.android.kozuzen.net.request.post;

import jp.kozu_osaka.android.kozuzen.net.argument.post.SendUsageDataArguments;

public final class SendUsageDataRequest extends PostRequest {

    public static final int ERROR_CODE_FAILURE_REGISTER = 1201;

    public SendUsageDataRequest(SendUsageDataArguments args) {
        super(RequestType.REGISTER_USAGE_DATA, args);
    }
}
