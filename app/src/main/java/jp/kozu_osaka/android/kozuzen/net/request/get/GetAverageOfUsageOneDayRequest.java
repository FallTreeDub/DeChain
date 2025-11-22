package jp.kozu_osaka.android.kozuzen.net.request.get;

import com.google.gson.JsonElement;

import jp.kozu_osaka.android.kozuzen.net.argument.get.GetAverageOfUsageOneDayArguments;

/**
 *
 */
public final class GetAverageOfUsageOneDayRequest extends GetRequest<Integer> {

    public GetAverageOfUsageOneDayRequest(GetAverageOfUsageOneDayArguments args) {
        super(RequestType.GET_AVERAGE_OF_USAGE_ONE_DAY, args);
    }

    @Override
    public Integer parseJsonResponse(JsonElement jsonElement) {
        return jsonElement.getAsInt();
    }
}
