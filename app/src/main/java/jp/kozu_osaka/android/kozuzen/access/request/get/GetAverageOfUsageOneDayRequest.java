package jp.kozu_osaka.android.kozuzen.access.request.get;

import com.google.gson.JsonElement;

import jp.kozu_osaka.android.kozuzen.access.argument.get.GetArguments;
import jp.kozu_osaka.android.kozuzen.access.argument.get.GetAverageOfUsageOneDayArguments;

/**
 *
 */
public final class GetAverageOfUsageOneDayRequest extends GetRequest<String> {

    public GetAverageOfUsageOneDayRequest(GetAverageOfUsageOneDayArguments args) {
        super(RequestType.GET_AVERAGE_OF_USAGE_ONE_DAY, args);
    }

    @Override
    public String parseJsonResponse(JsonElement jsonElement) {
        return jsonElement.getAsString();
    }
}
