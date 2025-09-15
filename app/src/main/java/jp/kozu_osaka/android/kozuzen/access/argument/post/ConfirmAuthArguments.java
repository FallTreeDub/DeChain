package jp.kozu_osaka.android.kozuzen.access.argument.post;

import java.util.Collections;
import java.util.Map;

import jp.kozu_osaka.android.kozuzen.access.argument.Arguments;

public final class ConfirmAuthArguments extends PostArguments {

    private static final String KEY_MAIL = "mail";
    private static final String KEY_ENTERED_CODE = "authCode";

    public ConfirmAuthArguments(String mail, String enteredCode) {
        super(Map.ofEntries(
                Map.entry(KEY_MAIL, Collections.singletonList(mail)),
                Map.entry(KEY_ENTERED_CODE, Collections.singletonList(enteredCode))
        ));
    }
}
