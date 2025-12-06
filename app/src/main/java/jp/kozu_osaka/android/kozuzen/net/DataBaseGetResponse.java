package jp.kozu_osaka.android.kozuzen.net;

import android.os.Parcel;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.jetbrains.annotations.NotNull;

public final class DataBaseGetResponse<T> {

    private static final String GET_RESPONSE_JSON_KEY_RESULT = "result";
    private static final String GET_RESPONSE_JSON_KEY_RESPONSE_CODE = "responseCode";
    private static final String GET_RESPONSE_JSON_KEY_MESSAGE = "message";

    private 

    public static DataBaseGetResponse<T> parse(@NotNull String responseJsonStr) {
        JsonObject jsonResponseRoot = JsonParser.parseString(responseJsonStr).getAsJsonObject();
        jsonResponseRoot.get(GET_RESPONSE_JSON_KEY_RESPONSE_CODE).getAsInt();
    }
}
