package jp.kozu_osaka.android.kozuzen.access.request.post;

import jp.kozu_osaka.android.kozuzen.access.argument.Arguments;
import jp.kozu_osaka.android.kozuzen.access.request.Request;

/**
 * データベースに対してPOSTリクエストを送るリクエスト。
 */
public class PostRequest extends Request {

    protected PostRequest(RequestType type, Arguments args) {
        super(type, args);
    }
}
