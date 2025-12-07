package jp.kozu_osaka.android.kozuzen.net.argument.post;

import java.util.Collections;
import java.util.Map;

import jp.kozu_osaka.android.kozuzen.net.request.post.PostRequest;
import jp.kozu_osaka.android.kozuzen.security.HashedString;

public class ConfirmResetPassAuthArguments extends PostArguments {
    private static final String KEY_MAIL = "mail";
    private static final String KEY_NEWPASS = "pass";
    private static final String KEY_ENTERED_CODE = "auth";

    public ConfirmResetPassAuthArguments(String mail, HashedString newPass, String enteredCode) {
        super(Map.ofEntries(
                Map.entry(KEY_MAIL, Collections.singletonList(mail)),
                Map.entry(KEY_NEWPASS, Collections.singletonList(newPass.toString())),
                Map.entry(KEY_ENTERED_CODE, Collections.singletonList(enteredCode))
        ));
    }
}
