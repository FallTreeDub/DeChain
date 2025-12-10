package jp.kozu_osaka.android.kozuzen.net.argument.post;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jp.kozu_osaka.android.kozuzen.net.usage.data.DailyUsageDatas;
import jp.kozu_osaka.android.kozuzen.net.usage.data.UsageData;

public final class SendUsageDataArguments extends PostArguments {

    private static final String KEY_MAIL = "mail";
    private static final String KEY_TIMESTAMP = "timeStamp";
    private static final String KEY_SNS_USAGES = "snsUsages";
    private static final String KEY_GAMES_USAGES = "gamesUsages";
    private static final String KEY_SNS_TOTAL_USAGE = "snsTotalUsage";
    private static final String KEY_GAME_TOTAL_USAGE = "gameTotalUsage";
    private static final String KEY_TOTAL_USAGE = "totalUsage";

    /**
     *
     */
    public SendUsageDataArguments(String mail, DailyUsageDatas datas) {
        super(Map.ofEntries(
                Map.entry(KEY_MAIL, Collections.singletonList(mail)),
                Map.entry(KEY_TIMESTAMP, Collections.singletonList(getDateStr())),
                Map.entry(KEY_SNS_USAGES, Collections.singletonList(generateUsageList(datas, UsageData.AppType.SNS))),
                Map.entry(KEY_GAMES_USAGES, Collections.singletonList(generateUsageList(datas, UsageData.AppType.GAMES))),
                Map.entry(KEY_SNS_TOTAL_USAGE, Collections.singletonList(String.valueOf(datas.getSNSMinutes()))),
                Map.entry(KEY_GAME_TOTAL_USAGE, Collections.singletonList(String.valueOf(datas.getGamesMinutes()))),
                Map.entry(KEY_TOTAL_USAGE, Collections.singletonList(String.valueOf(datas.getSNSMinutes() + datas.getGamesMinutes()))))
        );
    }

    private static String generateUsageList(DailyUsageDatas datas, UsageData.AppType type) {
        StringBuilder builder = new StringBuilder();
        for(UsageData data : datas.getUsageDatas()) {
            if(data.getAppType().equals(type)) {
                builder.append(String.format(Locale.JAPAN, "%s=%d", data.getAppName(), data.getUsageMinutes()));
            }
        }
        return builder.toString();
    }

    private static String getDateStr() {
        Calendar now = Calendar.getInstance(Locale.JAPAN);
        return String.format(Locale.JAPAN, "%d/%d/%d,%d:%d",
                now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1,
                now.get(Calendar.DAY_OF_MONTH), now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE)
        );
    }
}
