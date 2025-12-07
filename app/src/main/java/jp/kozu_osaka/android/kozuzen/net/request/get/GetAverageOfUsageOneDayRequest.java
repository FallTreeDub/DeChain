package jp.kozu_osaka.android.kozuzen.net.request.get;

import com.google.gson.JsonElement;

import jp.kozu_osaka.android.kozuzen.net.argument.get.GetAverageOfUsageOneDayArguments;

public final class GetAverageOfUsageOneDayRequest extends GetRequest<Integer> {

    public static final int ERROR_CODE_NOT_FOUND_TIMESTAMP = 7001;
    public static final int ERROR_CODE_NOT_FOUND_START_ROW = 7002;
    public static final int ERROR_CODE_NOT_FOUND_TOTALUSAGE = 7003;

    public GetAverageOfUsageOneDayRequest(GetAverageOfUsageOneDayArguments args) {
        super(RequestType.GET_AVERAGE_OF_USAGE_ONE_DAY, args);
    }

    @Override
    public Integer resultParse(JsonElement jsonElement) {
        return jsonElement.getAsInt();
    }
}
