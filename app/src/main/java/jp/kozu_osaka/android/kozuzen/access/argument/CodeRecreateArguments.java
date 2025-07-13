package jp.kozu_osaka.android.kozuzen.access.argument;

import java.util.Collections;
import java.util.Map;

public final class CodeRecreateArguments extends Arguments {

    private static final String KEY_MAIL = "mail";

    public CodeRecreateArguments(String mail) {
        super(Map.ofEntries(
                Map.entry(KEY_MAIL, Collections.singletonList(mail))
        ));
    }
}
