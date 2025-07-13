package jp.kozu_osaka.android.kozuzen.access.request;

import jp.kozu_osaka.android.kozuzen.access.argument.SendUsageDataArguments;

public final class SendUsageDataRequest extends Request {

    public SendUsageDataRequest(SendUsageDataArguments args) {
        super(RequestType.REGISTER_SNS_DATA, args);
    }
}
