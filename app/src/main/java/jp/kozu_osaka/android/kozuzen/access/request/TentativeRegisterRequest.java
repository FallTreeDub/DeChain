package jp.kozu_osaka.android.kozuzen.access.request;

import jp.kozu_osaka.android.kozuzen.access.argument.TentativeRegisterArguments;

public class TentativeRegisterRequest extends Request {

    public TentativeRegisterRequest(TentativeRegisterArguments args) {
        super(RequestType.REGISTER_TENTATIVE, args);
    }
}
