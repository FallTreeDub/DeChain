package jp.kozu_osaka.android.kozuzen.access;

import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DataBasePostResponse {

    private final int responseCode;
    private final String responseMessage;

    private DataBasePostResponse(int code, String msg) {
        responseCode = code;
        responseMessage = msg;
    }

    public static DataBasePostResponse parse(@NotNull String str) {
        Pattern p = Pattern.compile("\\[(\\d+)](.*)");
        Matcher m = p.matcher(str);

        if(m.matches()) {
            return new DataBasePostResponse(Integer.parseInt(m.group(1)), m.group(2));
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
