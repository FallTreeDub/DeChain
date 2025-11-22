package jp.kozu_osaka.android.kozuzen.net.argument.get;

import java.util.Map;

public final class GetTentativeExistenceArguments extends GetArguments {

    private static final String KEY_MAIL = "mail";

    public GetTentativeExistenceArguments(String mail) {
        super(Map.ofEntries(
                Map.entry(KEY_MAIL, mail)
        ));
    }
}
