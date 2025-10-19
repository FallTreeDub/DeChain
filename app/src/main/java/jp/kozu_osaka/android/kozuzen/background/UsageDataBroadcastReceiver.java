package jp.kozu_osaka.android.kozuzen.background;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import jp.kozu_osaka.android.kozuzen.ExperimentType;
import jp.kozu_osaka.android.kozuzen.KozuZen;
import jp.kozu_osaka.android.kozuzen.access.argument.get.GetAverageOfUsageOneDayArguments;
import jp.kozu_osaka.android.kozuzen.access.callback.GetAccessCallBack;
import jp.kozu_osaka.android.kozuzen.access.request.get.GetAverageOfUsageOneDayRequest;
import jp.kozu_osaka.android.kozuzen.exception.GetAccessException;
import jp.kozu_osaka.android.kozuzen.util.Logger;
import jp.kozu_osaka.android.kozuzen.util.PermissionsStatus;
import jp.kozu_osaka.android.kozuzen.R;
import jp.kozu_osaka.android.kozuzen.access.DataBaseAccessor;
import jp.kozu_osaka.android.kozuzen.access.DataBasePostResponse;
import jp.kozu_osaka.android.kozuzen.access.argument.post.SendUsageDataArguments;
import jp.kozu_osaka.android.kozuzen.access.callback.PostAccessCallBack;
import jp.kozu_osaka.android.kozuzen.access.request.post.SendUsageDataRequest;
import jp.kozu_osaka.android.kozuzen.exception.PostAccessException;
import jp.kozu_osaka.android.kozuzen.data.DailyUsageDatas;
import jp.kozu_osaka.android.kozuzen.data.UsageData;
import jp.kozu_osaka.android.kozuzen.internal.InternalRegisteredAccountManager;
import jp.kozu_osaka.android.kozuzen.internal.InternalUsageDataManager;
import jp.kozu_osaka.android.kozuzen.exception.NotFoundInternalAccountException;
import jp.kozu_osaka.android.kozuzen.util.NotificationProvider;

/**
 *
 */
public final class UsageDataBroadcastReceiver extends BroadcastReceiver {

    public static final int ALARM_REQUEST_CODE = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
        if(!PermissionsStatus.isAllowedAppUsageStats()) {
            NotificationProvider.sendNotification(
                    NotificationProvider.NotificationTitle.ON_BACKGROUND_ERROR_OCCURRED,
                    NotificationProvider.NotificationIcon.NONE,
                    R.string.notification_message_background_noPermission
            );
            return;
        }
        if(!PermissionsStatus.isAllowedNotification()) {
            NotificationProvider.sendNotification(
                    NotificationProvider.NotificationTitle.ON_BACKGROUND_ERROR_OCCURRED,
                    NotificationProvider.NotificationIcon.NONE,
                    R.string.notification_message_background_noPermission
            );
            return;
        }
        if(!InternalRegisteredAccountManager.isRegistered()) {
            KozuZen.createErrorReport(new NotFoundInternalAccountException("Not found a internal register account for registering a background error report."));
            return;
        }

        SendUsageDataRequest request = new SendUsageDataRequest(
                new SendUsageDataArguments(InternalRegisteredAccountManager.getMailAddress(), todayDatas)
        );
        //DataBaseに送信
        PostAccessCallBack callBack = new PostAccessCallBack(request) {
            @Override
            public void onSuccess() {}

            @Override
            public void onFailure(DataBasePostResponse response) {
                KozuZen.createErrorReport(new PostAccessException(response));
            }

            @Override
            public void onTimeOut(DataBasePostResponse response) {
                retry();
                KozuZen.createErrorReport(new PostAccessException(response));
            }
        };
        DataBaseAccessor.sendPostRequest(request, callBack);

        //internalに保存
        try {
            InternalUsageDataManager.addDailyDatas(createTodayUsageDatas());
        } catch (IOException e) {
            KozuZen.createErrorReport(e);
        }

        if(InternalRegisteredAccountManager.getExperimentType() == ExperimentType.TYPE_NON_NOTIFICATION) {
            return;
        }

        Calendar today = Calendar.getInstance();
        if(InternalRegisteredAccountManager.getExperimentType() == ExperimentType.TYPE_POSITIVE_WITH_SELF ||
                InternalRegisteredAccountManager.getExperimentType() == ExperimentType.TYPE_NEGATIVE_WITH_SELF) {
            DailyUsageDatas yesterdayDatas;
            try {
                //実験1日目は前日の使用時間データがInternalにたまっていないので前日との比較は不可
                yesterdayDatas = InternalUsageDataManager.getDataOf(today.get(Calendar.DAY_OF_MONTH - 1));
                if(yesterdayDatas == null) {
                    return;
                }
            } catch(IOException e) {
                KozuZen.createErrorReport(e);
            }
        } else {
            GetAverageOfUsageOneDayRequest getAveRequest = new GetAverageOfUsageOneDayRequest(
                    new GetAverageOfUsageOneDayArguments(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH))
            );
            GetAccessCallBack<String> getAccessCallBack = new GetAccessCallBack<>(getAveRequest) {
                @Override
                public void onSuccess(@NotNull String responseResult) {
                    int aveHour = Integer.parseInt(responseResult.split(":")[0]);
                    int aveMinute = Integer.parseInt(responseResult.split(":")[1]);

                }

                @Override
                public void onFailure() {
                    KozuZen.createErrorReport(new GetAccessException("Failed to get average of usage one day."));
                }

                @Override
                public void onTimeOut() {
                    retry();
                }
            };
            DataBaseAccessor.sendGetRequest(getAveRequest, getAccessCallBack);
        }
    }

    private void sendWithSelfNotification(DailyUsageDatas yesterdayDatas, DailyUsageDatas todayDatas) {

    }

    private void sendWithOtherNotification() {

    }

    private DailyUsageDatas createTodayUsageDatas() {
        UsageStatsManager usageManager = (UsageStatsManager)KozuZen.getInstance().getSystemService(Context.USAGE_STATS_SERVICE);
        PackageManager pm = KozuZen.getInstance().getPackageManager();

        //DeChain Usage Datas
        DailyUsageDatas todayDatas = DailyUsageDatas.create(Calendar.getInstance().get(Calendar.DAY_OF_MONTH));

        //Android Usage Datas
        //前日の20時から今日の19時59分59秒までの利用時間
        Calendar yesterdayEightPM = Calendar.getInstance();
        yesterdayEightPM.add(Calendar.DAY_OF_MONTH, -1);
        yesterdayEightPM.set(Calendar.HOUR_OF_DAY, 20);
        yesterdayEightPM.set(Calendar.MINUTE, 0);
        yesterdayEightPM.set(Calendar.SECOND, 0);
        yesterdayEightPM.set(Calendar.MILLISECOND, 0);
        Calendar todayEightPM = Calendar.getInstance();
        todayEightPM.set(Calendar.HOUR_OF_DAY, 19);
        todayEightPM.set(Calendar.MINUTE, 59);
        todayEightPM.set(Calendar.SECOND, 59);
        todayEightPM.set(Calendar.MILLISECOND, 0);
        List<UsageStats> stats = usageManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                yesterdayEightPM.getTimeInMillis(),
                todayEightPM.getTimeInMillis()
        );

        try {
            for(UsageStats stat : stats) {
                ApplicationInfo info = pm.getApplicationInfo(stat.getPackageName(), 0);
                String appName = pm.getApplicationLabel(info).toString();
                if(todayDatas.contains(appName)) {
                    todayDatas.getFrom(appName).addUsageTimeMillis(stat.getTotalTimeInForeground());
                    continue;
                }

                if(info.category == ApplicationInfo.CATEGORY_SOCIAL) {
                    UsageData data = new UsageData(UsageData.AppType.SNS, appName, stat.getTotalTimeInForeground());
                    todayDatas.add(data);
                } else if(info.category == ApplicationInfo.CATEGORY_GAME) {
                    UsageData data = new UsageData(UsageData.AppType.GAMES, appName, stat.getTotalTimeInForeground());
                    todayDatas.add(data);
                }
            }
        } catch(Exception e) {
            KozuZen.createErrorReport(e);
        }
        return todayDatas;
    }
}
