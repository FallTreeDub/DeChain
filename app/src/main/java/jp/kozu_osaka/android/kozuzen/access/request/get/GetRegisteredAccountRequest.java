package jp.kozu_osaka.android.kozuzen.access.request.get;

import jp.kozu_osaka.android.kozuzen.access.argument.get.GetRegisteredAccountArguments;

public final class GetRegisteredAccountRequest extends GetRequest {

    public GetRegisteredAccountRequest(GetRegisteredAccountArguments args) {
        super(RequestType.GET_REGISTERED_ACCOUNT, args);
    }
}
