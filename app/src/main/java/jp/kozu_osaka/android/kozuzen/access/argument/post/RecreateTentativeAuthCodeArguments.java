package jp.kozu_osaka.android.kozuzen.access.argument.post;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import jp.kozu_osaka.android.kozuzen.access.argument.Arguments;

public final class RecreateTentativeAuthCodeArguments extends Arguments {

    private static final String KEY_MAIL = "mail";

    public RecreateTentativeAuthCodeArguments(String mail) {
        super(Map.ofEntries(
                Map.entry(KEY_MAIL, Collections.singletonList(mail))
        ));
    }
}
