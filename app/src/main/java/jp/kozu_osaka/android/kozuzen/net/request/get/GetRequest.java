package jp.kozu_osaka.android.kozuzen.net.request.get;

import android.net.Uri;

import com.google.gson.JsonElement;

import java.util.Map;

import jp.kozu_osaka.android.kozuzen.net.argument.get.GetArguments;
import jp.kozu_osaka.android.kozuzen.net.request.Request;
import jp.kozu_osaka.android.kozuzen.security.DeChainSignatures;
import jp.kozu_osaka.android.kozuzen.security.Secrets;

public abstract class GetRequest<T> extends Request {

    protected final GetArguments arguments;

    protected GetRequest(RequestType type, GetArguments args) {
        super(type);
        this.arguments = args;
    }

    /**
     * データベースからのjsonでのレスポンスをこのリクエストの期待する型にパージする。
     * @return
     */
    public abstract T parseJsonResponse(JsonElement jsonElement);
}
