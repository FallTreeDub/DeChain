package jp.kozu_osaka.android.kozuzen.access.argument;

import java.util.Collections;
import java.util.Map;

public final class ConfirmAuthArguments extends Arguments {

    private static final String KEY_MAIL = "mail";
    private static final String KEY_ENTERED_CODE = "authCode";

    public ConfirmAuthArguments(String mail, String enteredCode) {
        super(Map.ofEntries(
                Map.entry(KEY_MAIL, Collections.singletonList(mail)),
                Map.entry(KEY_ENTERED_CODE, Collections.singletonList(enteredCode))
        ));
    }
}
