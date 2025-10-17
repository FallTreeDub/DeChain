package jp.kozu_osaka.android.kozuzen.background;

import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Process;

import androidx.annotation.Nullable;
import androidx.work.ListenableWorker;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import jp.kozu_osaka.android.kozuzen.KozuZen;
import jp.kozu_osaka.android.kozuzen.R;
import jp.kozu_osaka.android.kozuzen.access.DataBaseAccessor;
import jp.kozu_osaka.android.kozuzen.access.DataBasePostResponse;
import jp.kozu_osaka.android.kozuzen.access.argument.post.SendUsageDataArguments;
import jp.kozu_osaka.android.kozuzen.access.callback.PostAccessCallBack;
import jp.kozu_osaka.android.kozuzen.access.request.post.SendUsageDataRequest;
import jp.kozu_osaka.android.kozuzen.data.DailyUsageDatas;
import jp.kozu_osaka.android.kozuzen.data.UsageData;
import jp.kozu_osaka.android.kozuzen.internal.InternalBackgroundErrorReport;
import jp.kozu_osaka.android.kozuzen.internal.InternalRegisteredAccount;
import jp.kozu_osaka.android.kozuzen.internal.InternalUsageDataManager;
import jp.kozu_osaka.android.kozuzen.internal.exception.NotFoundInternalAccountException;
import jp.kozu_osaka.android.kozuzen.notification.NotificationProvider;

public final class UsageDataBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if(!isAllowedAppUsageStats()) {
            NotificationProvider.sendNotification(
                    NotificationProvider.NotificationTitle.ON_BACKGROUND_ERROR_OCCURRED,
                    R.string.notification_message_background_noPermission
            );
            return;
        }
        if(InternalRegisteredAccount.get() == null) {
            KozuZen.createErrorReport(new NotFoundInternalAccountException("Not found a internal register account for registering a background error report."));
            return;
        }

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
            return;
        }

        SendUsageDataRequest request = new SendUsageDataRequest(new SendUsageDataArguments(InternalRegisteredAccount.get().getMailAddress(), todayDatas));

        //DataBaseに送信
        PostAccessCallBack callBack = new PostAccessCallBack(request) {
            @Override
            public void onSuccess() {}

            @Override
            public void onFailure(@Nullable DataBasePostResponse response) {}

            @Override
            public void onTimeOut(DataBasePostResponse response) {
                retry();
            }
        };

        DataBaseAccessor.sendPostRequest(request, callBack);
        //internalに保存
        try {
            InternalUsageDataManager.addDailyDatas(todayDatas);
        } catch (IOException e) {
            KozuZen.createErrorReport(e);
        }

        //通知送る(Part2-5の場合)
        //todo: 追加
        if() {

        }
    }

    private boolean isAllowedAppUsageStats() {
        AppOpsManager aoManager = (AppOpsManager)KozuZen.getInstance().getSystemService(Context.APP_OPS_SERVICE);
        int mode = aoManager.unsafeCheckOp(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), KozuZen.getInstance().getPackageName());
        if(mode != AppOpsManager.MODE_DEFAULT) return true;
        return KozuZen.getInstance().checkPermission("android.permission.PACKAGE_USAGE_STATS", android.os.Process.myPid(), Process.myUid())
                == PackageManager.PERMISSION_GRANTED;
    }
}
