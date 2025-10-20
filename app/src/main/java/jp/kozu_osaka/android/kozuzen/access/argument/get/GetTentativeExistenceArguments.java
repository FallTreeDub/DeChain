package jp.kozu_osaka.android.kozuzen.access.argument.get;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import jp.kozu_osaka.android.kozuzen.access.argument.Arguments;

public final class GetTentativeExistenceArguments extends GetArguments {

    private static final String KEY_MAIL = "mail";

    public GetTentativeExistenceArguments(String mail) {
        super(Map.ofEntries(
                Map.entry(KEY_MAIL, mail)
        ));
    }
}
