package jp.kozu_osaka.android.kozuzen.access.request.get;

import jp.kozu_osaka.android.kozuzen.access.argument.get.GetTentativeAccountArguments;

public final class GetTentativeAccountRequest extends GetRequest {

    public GetTentativeAccountRequest(GetTentativeAccountArguments args) {
        super(RequestType.GET_TENTATIVE_ACCOUNT, args);
    }
}
