package jp.kozu_osaka.android.kozuzen.net;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DataBasePostResponse {

    private final int responseCode;
    @Nullable
    private final String responseMessage;

    private DataBasePostResponse(int code, @Nullable String msg) {
        responseCode = code;
        responseMessage = msg;
    }

    public static DataBasePostResponse parse(@NotNull String str) {
        String[] splited = str.split(",", 1);

        if(splited.length == 2) {
            try {
                int responseCode = Integer.parseInt(splited[0]);
                return new DataBasePostResponse(responseCode, splited[1]);
            } catch(NumberFormatException e) {
                throw new IllegalArgumentException("データベースからのレスポンス文字列ではない:" + str);
            }
        } else if(splited.length == 1) {
            try {
                int responseCode = Integer.parseInt(splited[0]);
                return new DataBasePostResponse(responseCode, null);
            } catch(NumberFormatException e) {
                throw new IllegalArgumentException("データベースからのレスポンス文字列ではない:" + str);
            }
        }
        throw new IllegalArgumentException("データベースからのレスポンス文字列ではない:" + str);
    }

    public int getResponseCode() {
        return responseCode;
    }

    public String getResponseMessage() {
        return responseMessage;
    }
}
