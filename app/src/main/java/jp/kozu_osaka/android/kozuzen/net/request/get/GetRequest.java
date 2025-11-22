package jp.kozu_osaka.android.kozuzen.net.request.get;

import android.net.Uri;

import com.google.gson.JsonElement;

import java.util.Map;

import jp.kozu_osaka.android.kozuzen.net.argument.get.GetArguments;
import jp.kozu_osaka.android.kozuzen.net.request.Request;
import jp.kozu_osaka.android.kozuzen.security.Secrets;

public abstract class GetRequest<T> extends Request {

    private static final String URL_QUERY_SIGNATURE = "signature";
    private static final String URL_QUERY_OPERATION_ID = "operationID";

    protected final GetArguments arguments;

    protected GetRequest(RequestType type, GetArguments args) {
        super(type);
        this.arguments = args;
    }

    public String toQueryURL() {
        Uri.Builder uriBuilder = Uri.parse(Secrets.ACCESS_QUERY_URL).buildUpon();
        uriBuilder.appendQueryParameter(URL_QUERY_SIGNATURE, "t");
        uriBuilder.appendQueryParameter(URL_QUERY_OPERATION_ID, String.valueOf(this.type.getRequestCode()));
        for(Map.Entry<String, String> entry : this.arguments.toMap().entrySet()) {
            uriBuilder.appendQueryParameter(entry.getKey(), entry.getValue());
        }
        return uriBuilder.toString();
    }

    /**
     * データベースからのjsonでのレスポンスをこのリクエストの期待する型にパージする。
     * @return
     */
    public abstract T parseJsonResponse(JsonElement jsonElement);
}
