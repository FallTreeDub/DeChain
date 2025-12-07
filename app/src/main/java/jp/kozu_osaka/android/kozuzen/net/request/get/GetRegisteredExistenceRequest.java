package jp.kozu_osaka.android.kozuzen.net.request.get;

import com.google.gson.JsonElement;

import jp.kozu_osaka.android.kozuzen.net.argument.get.GetRegisteredExistenceArguments;

public final class GetRegisteredExistenceRequest extends GetRequest<Integer> {

    public static final int ERROR_CODE_NOT_FOUND_PASSCOLUMN = 6101;
    public static final int ERROR_CODE_PASS_INCORRECT = 6102;

    public GetRegisteredExistenceRequest(GetRegisteredExistenceArguments args) {
        super(RequestType.GET_REGISTERED_ACCOUNT_EXISTENCE, args);
    }

    @Override
    public Integer resultParse(JsonElement jsonElement) {
        return jsonElement.getAsInt();
    }
}
