package jp.kozu_osaka.android.kozuzen.access.request;

import jp.kozu_osaka.android.kozuzen.access.argument.Arguments;
import jp.kozu_osaka.android.kozuzen.access.argument.CodeRecreateArguments;
import jp.kozu_osaka.android.kozuzen.security.SixNumberCode;

public final class CodeRecreateRequest extends Request {

    public CodeRecreateRequest(SixNumberCode.CodeType codeType, CodeRecreateArguments args) {
        super(codeType.getRecreateRequestType(), args);
    }
}
