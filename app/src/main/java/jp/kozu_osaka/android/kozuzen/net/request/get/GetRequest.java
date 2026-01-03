package jp.kozu_osaka.android.kozuzen.net.request.get;

import com.google.gson.JsonElement;

import java.util.Map;

import jp.kozu_osaka.android.kozuzen.net.argument.get.GetArguments;
import jp.kozu_osaka.android.kozuzen.net.request.Request;
import jp.kozu_osaka.android.kozuzen.security.Secrets;

/**
 * DeChainデータベースとのHTTP GETリクエストを送る際の通信内容。
 * @param <T> データベースから受け取る値の型。
 */
public abstract class GetRequest<T> extends Request {

    protected final GetArguments arguments;
    private static final String GET_REQUEST_PARAM_KEY_SIGNATURE = "signatures";
    private static final String GET_REQUEST_PARAM_KEY_OPERATION_ID = "operationID";

    protected GetRequest(RequestType type, GetArguments args) {
        super(type);
        this.arguments = args;
    }

    /**
     * {@link GetRequest<T>}が持つ{@link GetArguments<T>}をURLパラメータに変換する。
     * また、同時にアプリ署名をURLパラメータに入れる。
     */
    public String toURLParam() {
        //アプリ署名
        String[] appSigns = Secrets.getSignatureHexStringArray();
        StringBuilder signsBuilder = new StringBuilder();
        if(appSigns != null) {
            for(String sign : appSigns) {
                signsBuilder.append(sign).append(",");
            }
            signsBuilder.deleteCharAt(signsBuilder.lastIndexOf(","));
        }

        StringBuilder b = new StringBuilder();
        b.append("?");

        //URL param方式に変換
        for(Map.Entry<String, String> entry : this.arguments.toMap().entrySet()) {
            b.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }
        b.append(GET_REQUEST_PARAM_KEY_OPERATION_ID).append("=").append(this.type.getRequestCode()).append("&");
        b.append(GET_REQUEST_PARAM_KEY_SIGNATURE).append("=").append(signsBuilder);
        return b.toString();
    }

    public abstract T resultParse(JsonElement resultJsonElem);
}
