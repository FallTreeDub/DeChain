package jp.kozu_osaka.android.kozuzen.net.request.get;

import com.google.gson.JsonElement;

import jp.kozu_osaka.android.kozuzen.net.argument.get.GetLatestVersionCodeArguments;

public final class GetLatestVersionCodeRequest extends GetRequest<Integer> {

    public GetLatestVersionCodeRequest(GetLatestVersionCodeArguments args) {
        super(RequestType.GET_LATEST_VERSION_CODE, args);
    }

    @Override
    public Integer parseJsonResponse(JsonElement jsonElement) {
        return jsonElement.getAsInt();
    }
}
