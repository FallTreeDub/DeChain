package jp.kozu_osaka.android.kozuzen.data;

import java.util.concurrent.TimeUnit;

/**
 * 1日ごとのゲームアプリの使用時間またはSNSの利用時間の1アプリのデータ。
 */
public final class UsageData {

    private final String appName;
    private final AppType appType;
    private final int usageHours;
    private final int usageMinutes;

    public UsageData(AppType appType, String appName, long usageTimeMillis) {
        this.appName = appName;
        this.appType = appType;
        int usageTimeInSeconds = (int)TimeUnit.MILLISECONDS.toSeconds(usageTimeMillis);
        this.usageHours = usageTimeInSeconds / 3600;
        this.usageMinutes = (usageTimeInSeconds - this.usageHours * 3600) / 60;
    }

    public UsageData(AppType appType, String appName, int usageHours, int usageMinutes) {
        this(appType, appName, TimeUnit.HOURS.toMillis(usageHours) + TimeUnit.MINUTES.toMillis(usageMinutes));
    }

    public AppType getAppType() {
        return this.appType;
    }

    public String getAppName() {
        return this.appName;
    }

    /**
     * 使用時間のうち、時間(hours)の部分のみを返す。
     * @return
     */
    public int getUsageHours() {
        return this.usageHours;
    }

    /**
     * 使用時間のうち、分(minutes)の部分のみを返す。
     * @return
     */
    public int getUsageMinutes() {
        return this.usageMinutes;
    }

    public enum AppType {
        SNS(0),
        GAMES(1);

        private final int ID;

        AppType(int id) {
            this.ID = id;
        }

        public int getId() {
            return this.ID;
        }

        public static AppType from(int id) {
            for(AppType t : values()) {
                if(t.getId() == id) {
                    return t;
                }
            }
            return null;
        }
    }
}
