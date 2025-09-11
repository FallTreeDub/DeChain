package jp.kozu_osaka.android.kozuzen.access.request.get;

import android.net.Uri;

import com.google.gson.JsonElement;

import java.net.MalformedURLException;
import java.net.URL;

import jp.kozu_osaka.android.kozuzen.access.argument.Arguments;
import jp.kozu_osaka.android.kozuzen.access.argument.get.GetArguments;
import jp.kozu_osaka.android.kozuzen.access.request.Request;
import jp.kozu_osaka.android.kozuzen.security.Secrets;

public abstract class GetRequest<T> extends Request {

    private static final String URL_QUERY_SIGNATURE = "signature";
    private static final String URL_QUERY_OPERATION_ID = "operationID";

    protected GetRequest(RequestType type, GetArguments args) {
        super(type, args);
    }

    public String toQueryURL() {
        Uri.Builder uriBuilder = Uri.parse(Secrets.ACCESS_QUERY_URL).buildUpon();
        uriBuilder.appendQueryParameter(URL_QUERY_SIGNATURE, "t");
        uriBuilder.appendQueryParameter(URL_QUERY_OPERATION_ID, String.valueOf(this.type.getRequestCode()));
        for() {

        }
    }

    /**
     * データベースからのjsonでのレスポンスをこのリクエストの期待する型にパージする。
     * @return
     */
    public abstract T parseJsonResponse(JsonElement jsonElement);
}
