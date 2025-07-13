package jp.kozu_osaka.android.kozuzen.data;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * 1日当たりのすべてのアプリの使用時間を記録し、SNSとゲームの使用時間のそれぞれの合計を記録する。
 */
public final class DailyUsageDatas {

    private final int dayOfMonth;
    
    private int SNSHours = 0;
    private int SNSMinutes = 0;
    private int gamesHours = 0;
    private int gamesMinutes = 0;

    private final List<UsageData> usageDatas = new ArrayList<>();

    private DailyUsageDatas(int dayOfMonth) {
        this.dayOfMonth = dayOfMonth;
    }

    /**
     * {@code DailyUsageDatas}をインスタンス化する。
     * @param dayOfMonth 使用データの日。
     * @return インスタンス。日付が無効な場合は{@code null}を返す。
     */
    public static DailyUsageDatas create(int dayOfMonth) {
        if(!(1 <= dayOfMonth && dayOfMonth <= 31)) return null;
        return new DailyUsageDatas(dayOfMonth);
    }

    public void add(UsageData data) {
        if(data.getAppType().equals(UsageData.AppType.SNS)) {
            SNSHours += data.getUsageHours();
            SNSMinutes += data.getUsageMinutes();
        } else if(data.getAppType().equals(UsageData.AppType.GAMES)) {
            gamesHours += data.getUsageHours();
            gamesMinutes += data.getUsageMinutes();
        }
        this.usageDatas.add(data);
    }

    public int getSNSHours() {
        return SNSHours;
    }

    public int getSNSMinutes() {
        return SNSMinutes;
    }

    public int getGamesHours() {
        return gamesHours;
    }

    public int getGamesMinutes() {
        return gamesMinutes;
    }

    public List<UsageData> getUsageDatas() {
        return this.usageDatas;
    }

    public int getDayOfMonth() {
        return this.dayOfMonth;
    }
}
