package jp.kozu_osaka.android.kozuzen.access.request.get;

import com.google.gson.JsonElement;

import jp.kozu_osaka.android.kozuzen.access.argument.Arguments;
import jp.kozu_osaka.android.kozuzen.access.argument.get.GetRegisteredExistenceArguments;
import jp.kozu_osaka.android.kozuzen.access.argument.get.GetTentativeExistenceArguments;

public final class GetTentativeExistenceRequest extends GetRequest<Boolean> {

    public GetTentativeExistenceRequest(GetTentativeExistenceArguments args) {
        super(RequestType.GET_TENTATIVE_ACCOUNT_EXISTENCE, args);
    }

    @Override
    public Boolean parseJsonResponse(JsonElement jsonElement) {
        return jsonElement.getAsBoolean();
    }
}
