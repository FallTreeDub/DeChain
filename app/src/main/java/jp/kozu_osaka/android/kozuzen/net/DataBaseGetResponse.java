package jp.kozu_osaka.android.kozuzen.net;

import android.os.Parcel;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * DeChainデータベースからのJSONでの応答内容をjavaインスタンスとしてまとめる。
 */
public final class DataBaseGetResponse {

    private static final String GET_RESPONSE_JSON_KEY_RESULT = "result";
    private static final String GET_RESPONSE_JSON_KEY_RESPONSE_CODE = "responseCode";
    private static final String GET_RESPONSE_JSON_KEY_MESSAGE = "message";

    private final int responseCode;
    @Nullable
    private final String message;
    private final JsonElement result;

    private DataBaseGetResponse(int responseCode, @Nullable String msg, JsonElement result) {
        this.responseCode = responseCode;
        this.message = msg;
        this.result = result;
    }

    /**
     * データベースからのJSON応答を、javaインスタンスに変換する。
     * @param responseJsonStr データベースからのJSON応答。
     * @throws IllegalArgumentException データベースからの返答がJSONの形式ではないとき。
     */
    public static DataBaseGetResponse parse(@NotNull String responseJsonStr) throws IllegalArgumentException {
        JsonObject jsonResponseRoot;
        try {
            jsonResponseRoot = JsonParser.parseString(responseJsonStr).getAsJsonObject();
        } catch(JsonParseException e) {
            throw new IllegalArgumentException("データベースからのレスポンス文字列ではない:" + responseJsonStr);
        }
        int responseCode = Objects.requireNonNull(jsonResponseRoot.get(GET_RESPONSE_JSON_KEY_RESPONSE_CODE), () -> { throw new IllegalArgumentException("データベースからのレスポンス文字列ではない:" + responseJsonStr); })
                .getAsInt();
        String msg = jsonResponseRoot.has(GET_RESPONSE_JSON_KEY_MESSAGE) ? jsonResponseRoot.get(GET_RESPONSE_JSON_KEY_MESSAGE).getAsString() : null;
        JsonElement resultElem = jsonResponseRoot.has(GET_RESPONSE_JSON_KEY_RESULT) ? jsonResponseRoot.get(GET_RESPONSE_JSON_KEY_RESULT) : null;
        return new DataBaseGetResponse(responseCode, msg, resultElem);
    }

    /**
     * 応答コードを返す。
     * @return
     */
    public int getResponseCode() {
        return responseCode;
    }

    @Nullable
    public String getMessage() {
        return message;
    }

    public JsonElement getResultJsonElement() {
        return result;
    }
}
