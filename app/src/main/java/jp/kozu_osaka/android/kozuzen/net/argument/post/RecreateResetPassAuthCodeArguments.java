package jp.kozu_osaka.android.kozuzen.net.argument.post;

import java.util.Collections;
import java.util.Map;

public final class RecreateResetPassAuthCodeArguments extends PostArguments {

    private static final String KEY_MAIL = "mail";

    public RecreateResetPassAuthCodeArguments(String mail) {
        super(Map.ofEntries(
                Map.entry(KEY_MAIL, Collections.singletonList(mail))
        ));
    }
}
