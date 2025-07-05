package jp.kozu_osaka.android.kozuzen.access.request;

import jp.kozu_osaka.android.kozuzen.access.argument.ConfirmAuthArguments;
import jp.kozu_osaka.android.kozuzen.security.SixNumberCode;

public class ConfirmAuthRequest extends Request {

    public ConfirmAuthRequest(SixNumberCode.CodeType codeType, ConfirmAuthArguments args) {
        super(codeType.getConfirmAuthRequestType(), args);
    }
}
