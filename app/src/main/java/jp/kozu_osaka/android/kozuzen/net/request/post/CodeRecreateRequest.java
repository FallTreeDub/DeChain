package jp.kozu_osaka.android.kozuzen.net.request.post;

import jp.kozu_osaka.android.kozuzen.net.argument.post.CodeRecreateArguments;
import jp.kozu_osaka.android.kozuzen.security.SixNumberCode;

public final class CodeRecreateRequest extends PostRequest {

    public CodeRecreateRequest(SixNumberCode.CodeType codeType, CodeRecreateArguments args) {
        super(codeType.getRecreateRequestType(), args);
    }
}
