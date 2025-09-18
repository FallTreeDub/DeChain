package jp.kozu_osaka.android.kozuzen.data;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * 1日当たりのすべてのアプリの使用時間を記録し、SNSとゲームの使用時間のそれぞれの合計を記録する。
 */
public final class DailyUsageDatas {

    private final int dayOfMonth;
    
    private int SNSHours = 0;
    private int SNSMinutes = 0;
    private int gamesHours = 0;
    private int gamesMinutes = 0;

    private final Set<UsageData> usageDatas = new HashSet<>();

    private DailyUsageDatas(int dayOfMonth) {
        this.dayOfMonth = dayOfMonth;
    }

    /**
     * {@code DailyUsageDatas}をインスタンス化する。
     * @param dayOfMonth 使用データの日。
     * @exception IllegalArgumentException 日付が無効な場合。
     * @return インスタンス。
     */
    public static DailyUsageDatas create(int dayOfMonth) throws IllegalArgumentException {
        if(!(1 <= dayOfMonth && dayOfMonth <= 31)) throw new IllegalArgumentException("Day of month is invalid.");
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

    /**
     * すべての使用データのうち、使用アプリの名前が{@code appName}であるデータが存在するか判定する。
     * @param appName データの存在を判定するアプリの名前。
     * @return 使用アプリの存在有無。
     */
    public boolean contains(String appName) {
        return this.usageDatas.stream().anyMatch(d -> d.getAppName().equals(appName));
    }

    /**
     * 使用したアプリ名が{@code appName}に等しい{@code UsageData}を返す。
     * @param appName アプリ名。
     * @return {@code UsageData}。存在しない場合は{@code null}が返される。
     */
    @Nullable
    public UsageData getFrom(String appName) {
        for(UsageData d : this.usageDatas) {
            if(d.getAppName().equals(appName)) {
                return d;
            }
        }
        return null;
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

    public Set<UsageData> getUsageDatas() {
        return this.usageDatas;
    }

    public int getDayOfMonth() {
        return this.dayOfMonth;
    }
}
