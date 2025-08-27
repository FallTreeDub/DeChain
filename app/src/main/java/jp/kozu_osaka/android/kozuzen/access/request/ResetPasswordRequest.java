package jp.kozu_osaka.android.kozuzen.access.request;

import jp.kozu_osaka.android.kozuzen.access.argument.ResetPasswordArguments;

public final class ResetPasswordRequest extends Request {

    public ResetPasswordRequest(ResetPasswordArguments args) {
        super(RequestType.REQUEST_RESET_PASS, args);
    }
}
