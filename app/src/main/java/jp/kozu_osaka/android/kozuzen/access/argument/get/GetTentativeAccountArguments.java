package jp.kozu_osaka.android.kozuzen.access.argument.get;

import java.util.Collections;
import java.util.Map;

import jp.kozu_osaka.android.kozuzen.access.argument.Arguments;
import jp.kozu_osaka.android.kozuzen.security.HashedString;

public final class GetTentativeAccountArguments extends Arguments {

    private static final String KEY_MAIL = "mail";
    private static final String KEY_PASS = "pass";

    public GetTentativeAccountArguments(String mail, HashedString pass) {
        super(Map.ofEntries(
                Map.entry(KEY_MAIL, Collections.singletonList(mail)),
                Map.entry(KEY_PASS, Collections.singletonList(pass.toString()))
        ));
    }
}
