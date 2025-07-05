package jp.kozu_osaka.android.kozuzen.access.argument;

import java.util.Map;

public class CodeRecreateArguments extends Arguments {

    private static final String KEY_MAIL = "mail";

    public CodeRecreateArguments(String mail) {
        super(Map.ofEntries(
                Map.entry(KEY_MAIL, mail)
        ));
    }
}
