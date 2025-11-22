package jp.kozu_osaka.android.kozuzen.net.request.post;

import jp.kozu_osaka.android.kozuzen.net.argument.post.SendUsageDataArguments;

public final class SendUsageDataRequest extends PostRequest {

    public SendUsageDataRequest(SendUsageDataArguments args) {
        super(RequestType.REGISTER_USAGE_DATA, args);
    }
}
