package jp.kozu_osaka.android.kozuzen.access.argument.get;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import jp.kozu_osaka.android.kozuzen.access.argument.Arguments;
import jp.kozu_osaka.android.kozuzen.security.HashedString;

public final class GetRegisteredExistenceArguments extends GetArguments {

    private static final String KEY_MAIL = "mail";
    private static final String KEY_PASS = "pass";

    public GetRegisteredExistenceArguments(@NotNull String mail, HashedString pass) {
        super(Map.ofEntries(
                Map.entry(KEY_MAIL, mail),
                Map.entry(KEY_PASS, pass.toString())
        ));
    }
}
