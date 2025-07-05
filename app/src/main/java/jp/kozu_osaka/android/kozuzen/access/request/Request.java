package jp.kozu_osaka.android.kozuzen.access.request;

import jp.kozu_osaka.android.kozuzen.access.argument.Arguments;

/**
 * DeChainデータベースへの書き込みリクエストの実体。
 */
public class Request {

    private final RequestType type;
    private final Arguments arguments;

    protected Request(RequestType type, Arguments args) {
        this.type = type;
        this.arguments = args;
    }

    public RequestType getType() {
        return this.type;
    }

    public Arguments getArguments() {
        return this.arguments;
    }

    public enum RequestType {
        REGISTER_TENTATIVE(0),
        REGISTER(1),
        REGISTER_SNS_DATA(2),
        REGISTER_POST_SURVEY(5),
        SET_TENTATIVE_AUTHCODE_AUTHED(7),
        SET_TENTATIVE_AUTHCODE_RECREATE(8),
        SET_REQUEST_RESET_PASS(9),
        SET_RESET_PASS_AUTHCODE_AUTHED(10),
        SET_RESET_PASS_AUTHCODE_RECREATE(11);

        private final int REQUEST_CODE;
        RequestType(int requestCode) {
            this.REQUEST_CODE = requestCode;
        }

        public int getRequestCode() {
            return this.REQUEST_CODE;
        }
    }
}
