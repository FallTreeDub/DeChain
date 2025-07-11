package jp.kozu_osaka.android.kozuzen.access.argument;

import java.util.Locale;
import java.util.Map;

import jp.kozu_osaka.android.kozuzen.data.DailyUsageDatas;

public final class SendUsageDataArguments extends Arguments {

    private static final String KEY_MAIL = "mail";
    private static final String KEY_TIMESTAMP = "timeStamp";
    private static final String KEY_APPUSAGES = "appUsages";

    /**
     *
     */
    public SendUsageDataArguments(String mail, DailyUsageDatas datas) {
        super(Map.ofEntries(
                Map.entry(KEY_MAIL, mail),
                Map.entry(KEY_TIMESTAMP, String.format("%d/%d/%d,%d:%d", Locale.JAPAN, ))
        ));
    }
}
