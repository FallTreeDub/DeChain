package jp.kozu_osaka.android.kozuzen.access.request;

import jp.kozu_osaka.android.kozuzen.access.argument.Arguments;
import jp.kozu_osaka.android.kozuzen.access.argument.ResetPasswordArguments;

public class ResetPasswordRequest extends Request {

    public ResetPasswordRequest(ResetPasswordArguments args) {
        super(RequestType.SET_REQUEST_RESET_PASS, args);
    }
}
