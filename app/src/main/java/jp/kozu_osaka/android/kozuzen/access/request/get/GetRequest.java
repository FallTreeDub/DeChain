package jp.kozu_osaka.android.kozuzen.access.request.get;

import com.google.gson.JsonElement;

import jp.kozu_osaka.android.kozuzen.access.argument.Arguments;
import jp.kozu_osaka.android.kozuzen.access.request.Request;

public abstract class GetRequest<T> extends Request {

    protected GetRequest(RequestType type, Arguments args) {
        super(type, args);
    }

    /**
     * データベースからのjsonでのレスポンスをこのリクエストの期待する型にパージする。
     * @return
     */
    public abstract T parseJsonResponse(JsonElement jsonElement);
}
