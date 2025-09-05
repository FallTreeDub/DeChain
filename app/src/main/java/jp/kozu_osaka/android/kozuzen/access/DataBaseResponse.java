package jp.kozu_osaka.android.kozuzen.access;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DataBaseResponse {

    private final int responseCode;
    private final String responseMessage;

    public static final int responseOkCode = 200;
    public static final int responseUserErrorCode = 400;
    public static final int responseTimeoutCode = 408;

    private DataBaseResponse(int code, String msg) {
        responseCode = code;
        responseMessage = msg;
    }

    /**
     *
     * @param str
     * @return
     */
    public static DataBaseResponse parse(String str) {
        Pattern p = Pattern.compile("\\[(\\d+)](.*)");
        Matcher m = p.matcher(str);

        if(m.matches()) {
            return new DataBaseResponse(Integer.parseInt(m.group(1)), m.group(2));
        }
        throw new IllegalArgumentException("データベースからのレスポンス文字列ではない");
    }

    public int getResponseCode() {
        return responseCode;
    }

    public String getResponseMessage() {
        return responseMessage;
    }
}
