package jp.kozu_osaka.android.kozuzen.access.argument;

import java.util.Map;

public class ConfirmAuthArguments extends Arguments {

    private static final String KEY_MAIL = "mail";

    public ConfirmAuthArguments(String mail) {
        super(Map.ofEntries(
                Map.entry(KEY_MAIL, mail)
        ));
    }
}
