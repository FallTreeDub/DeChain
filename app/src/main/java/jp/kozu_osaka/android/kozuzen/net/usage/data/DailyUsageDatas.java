package jp.kozu_osaka.android.kozuzen.net.usage.data;

import androidx.annotation.Nullable;

import org.jetbrains.annotations.Range;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 1日当たりのすべてのアプリの使用時間を記録し、SNSとゲームの使用時間のそれぞれの合計を記録する。
 */
public final class DailyUsageDatas {

    private final int dayOfMonth;
    private final Set<UsageData> datas = new HashSet<>();

    private DailyUsageDatas(int dayOfMonth) {
        this.dayOfMonth = dayOfMonth;
    }

    /**
     * {@code DailyUsageDatas}をインスタンス化する。
     * @param dayOfMonth 使用データの日。
     * @exception IllegalArgumentException 日付が無効な場合。
     * @return インスタンス。
     */
    public static DailyUsageDatas create(@Range(from = 1, to = 31) int dayOfMonth) throws IllegalArgumentException {
        return new DailyUsageDatas(dayOfMonth);
    }

    /**
     * アプリ名が重複する場合、そのデータの内容(開いた回数、合計使用時間)を元からあったデータに加算する。
     * @param data
     */
    public void add(UsageData data) {
        if(contains(data.getAppName())) {
            UsageData former = getFrom(data.getAppName());
            this.datas.remove(former);
            data.addUsageTimeMillis(former.getUsageMillis());
        }
        this.datas.add(data);
    }

    /**
     * すべての使用データのうち、使用アプリの名前が{@code appName}であるデータが存在するか判定する。
     * @param appName データの存在を判定するアプリの名前。
     * @return 使用アプリの存在有無。
     */
    public boolean contains(String appName) {
        return this.datas.stream().anyMatch(d -> d.getAppName().equals(appName));
    }

    /**
     * 使用したアプリ名が{@code appName}に等しい{@code UsageData}を返す。
     * @param appName アプリ名。
     * @return {@code UsageData}。存在しない場合は{@code null}が返される。
     */
    @Nullable
    public UsageData getFrom(String appName) {
        if(!contains(appName)) return null;

        for(UsageData d : this.datas) {
            if(d.getAppName().equals(appName)) {
                return d;
            }
        }
        return null;
    }

    public int getSNSMinutes() {
        int sum = 0;
        for(UsageData d : this.datas) {
            if(d.getAppType().equals(UsageData.AppType.SNS)) {
                sum += d.getUsageMinutes();
            }
        }
        return sum;
    }

    public int getGamesMinutes() {
        int sum = 0;
        for(UsageData d : this.datas) {
            if(d.getAppType().equals(UsageData.AppType.GAMES)) {
                sum += d.getUsageMinutes();
            }
        }
        return sum;
    }

    /**
     * SNSとゲームの総使用時間をミリ秒で返す。
     * @return
     */
    public long getUsageTimeInMillis() {
        return TimeUnit.MINUTES.toMillis(getSNSMinutes() + getGamesMinutes());
    }

    public Set<UsageData> getUsageDatas() {
        return this.datas;
    }

    public int getDayOfMonth() {
        return this.dayOfMonth;
    }
}
