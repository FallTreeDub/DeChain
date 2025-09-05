package jp.kozu_osaka.android.kozuzen.access.request.post;

import jp.kozu_osaka.android.kozuzen.access.argument.post.ConfirmAuthArguments;
import jp.kozu_osaka.android.kozuzen.security.SixNumberCode;

public final class ConfirmAuthRequest extends PostRequest {

    public ConfirmAuthRequest(SixNumberCode.CodeType codeType, ConfirmAuthArguments args) {
        super(codeType.getConfirmAuthRequestType(), args);
    }
}
