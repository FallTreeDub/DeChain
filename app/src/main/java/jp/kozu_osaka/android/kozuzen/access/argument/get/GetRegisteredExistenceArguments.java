package jp.kozu_osaka.android.kozuzen.access.argument.get;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import jp.kozu_osaka.android.kozuzen.access.argument.Arguments;

public final class GetRegisteredExistenceArguments extends GetArguments {

    private static final String KEY_MAIL = "mail";

    public GetRegisteredExistenceArguments(@NotNull String mail) {
        super(Map.ofEntries(
                Map.entry(KEY_MAIL, mail)
        ));
    }
}
