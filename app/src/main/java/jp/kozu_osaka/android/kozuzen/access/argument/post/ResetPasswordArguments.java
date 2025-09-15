package jp.kozu_osaka.android.kozuzen.access.argument.post;

import java.util.Collections;
import java.util.Map;

import jp.kozu_osaka.android.kozuzen.access.argument.Arguments;
import jp.kozu_osaka.android.kozuzen.security.HashedString;

public final class ResetPasswordArguments extends PostArguments {

    private static final String KEY_MAIL = "mail";
    private static final String KEY_NEW_PASS = "newPassword";

    public ResetPasswordArguments(String mailAddress, HashedString newPassword) {
        super(Map.ofEntries(
                Map.entry(KEY_MAIL, Collections.singletonList(mailAddress)),
                Map.entry(KEY_NEW_PASS, Collections.singletonList(newPassword.toString()))
        ));
    }
}
