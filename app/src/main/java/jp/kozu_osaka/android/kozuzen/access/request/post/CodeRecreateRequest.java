package jp.kozu_osaka.android.kozuzen.access.request.post;

import jp.kozu_osaka.android.kozuzen.access.argument.post.CodeRecreateArguments;
import jp.kozu_osaka.android.kozuzen.security.SixNumberCode;

public final class CodeRecreateRequest extends PostRequest {

    public CodeRecreateRequest(SixNumberCode.CodeType codeType, CodeRecreateArguments args) {
        super(codeType.getRecreateRequestType(), args);
    }
}
