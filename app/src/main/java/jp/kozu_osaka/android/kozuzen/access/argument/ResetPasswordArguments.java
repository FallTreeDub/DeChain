package jp.kozu_osaka.android.kozuzen.access.argument;

import java.util.Map;

import jp.kozu_osaka.android.kozuzen.security.HashedString;

public class ResetPasswordArguments extends Arguments {

    private static final String KEY_MAIL = "mail";
    private static final String KEY_NEW_PASS = "pass";

    public ResetPasswordArguments(String mailAddress, HashedString newPassword) {
        super(Map.ofEntries(
                Map.entry(KEY_MAIL, mailAddress),
                Map.entry(KEY_NEW_PASS, newPassword.toString())
        ));
    }
}
