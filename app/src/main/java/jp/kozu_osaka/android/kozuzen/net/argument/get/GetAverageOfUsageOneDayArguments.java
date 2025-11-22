package jp.kozu_osaka.android.kozuzen.net.argument.get;

import org.jetbrains.annotations.Range;

import java.util.Map;

public final class GetAverageOfUsageOneDayArguments extends GetArguments {

    private static final String KEY_YEAR = "year";
    private static final String KEY_MONTH = "month";
    private static final String KEY_DAY_OF_MONTH = "dayOfMonth";

    public GetAverageOfUsageOneDayArguments(
            @Range(from = 0, to = Integer.MAX_VALUE) int targetYear,
            @Range(from = 1, to = 12) int targetMonth,
            @Range(from = 1, to = 31) int targetDayOfMonth) {
        super(Map.ofEntries(
                Map.entry(KEY_YEAR, String.valueOf(targetYear)),
                Map.entry(KEY_MONTH, String.valueOf(targetMonth)),
                Map.entry(KEY_DAY_OF_MONTH, String.valueOf(targetDayOfMonth))
        ));
    }
}
