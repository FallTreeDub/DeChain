package jp.kozu_osaka.android.kozuzen.access.request.get;

import com.google.gson.JsonElement;

import jp.kozu_osaka.android.kozuzen.access.argument.Arguments;
import jp.kozu_osaka.android.kozuzen.access.argument.get.GetRegisteredExistenceArguments;

public final class GetRegisteredExistenceRequest extends GetRequest<Boolean> {

    public GetRegisteredExistenceRequest(GetRegisteredExistenceArguments args) {
        super(RequestType.GET_REGISTERED_ACCOUNT_EXISTENCE, args);
    }

    @Override
    public Boolean parseJsonResponse(JsonElement jsonElement) {
        return jsonElement.getAsBoolean();
    }
}
