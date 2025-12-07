package jp.kozu_osaka.android.kozuzen.net.request.post;

import jp.kozu_osaka.android.kozuzen.net.argument.post.ConfirmResetPassAuthArguments;

public final class ConfirmResetPassAuthRequest extends PostRequest {

    public static final int ERROR_CODE_NOT_FOUND_PASSLINE_OR_CODELINE = 2101;
    public static final int ERROR_CODE_INCORRECT = 2102;

    public ConfirmResetPassAuthRequest(ConfirmResetPassAuthArguments args) {
        super(RequestType.CONFIRM_RESET_PASS_AUTHCODE, args);
    }
}
