package jp.kozu_osaka.android.kozuzen.access.argument;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jp.kozu_osaka.android.kozuzen.data.DailyUsageDatas;
import jp.kozu_osaka.android.kozuzen.data.UsageData;

public final class SendUsageDataArguments extends Arguments {

    private static final String KEY_MAIL = "mail";
    private static final String KEY_TIMESTAMP = "timeStamp";
    private static final String KEY_SNS_USAGES = "snsUsages";
    private static final String KEY_GAMES_USAGES = "gamesUsages";

    /**
     *
     */
    public SendUsageDataArguments(String mail, DailyUsageDatas datas) {
        super(Map.ofEntries(
                Map.entry(KEY_MAIL, Collections.singletonList(mail)),
                Map.entry(KEY_TIMESTAMP, Collections.singletonList(getDateStr())),
                Map.entry(KEY_SNS_USAGES, generateUsageList(datas, UsageData.AppType.SNS)),
                Map.entry(KEY_GAMES_USAGES, generateUsageList(datas, UsageData.AppType.GAMES))
        ));
    }

    private static List<String> generateUsageList(DailyUsageDatas datas, UsageData.AppType type) {
        List<String> list = new ArrayList<>();
        for(UsageData data : datas.getUsageDatas()) {
            if(data.getAppType().equals(type)) {
                list.add(String.format(Locale.JAPAN, "%s=%d:%d", data.getAppName(), data.getUsageHours(), data.getUsageMinutes()));
            }
        }
        return list;
    }

    private static String getDateStr() {
        Calendar now = Calendar.getInstance();
        return String.format(Locale.JAPAN, "%d/%d/%d,%d:%d",
                now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1,
                now.get(Calendar.DAY_OF_MONTH), now.get(Calendar.HOUR),
                now.get(Calendar.MINUTE)
        );
    }
}
