package jp.kozu_osaka.android.kozuzen.background;

import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Process;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.IOException;
import java.time.Duration;
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
import jp.kozu_osaka.android.kozuzen.internal.InternalBackgroundErrorReportManager;
import jp.kozu_osaka.android.kozuzen.internal.InternalRegisteredAccountManager;
import jp.kozu_osaka.android.kozuzen.internal.InternalUsageDataManager;
import jp.kozu_osaka.android.kozuzen.exception.NotFoundInternalAccountException;
import jp.kozu_osaka.android.kozuzen.util.NotificationProvider;

public final class UsageDataWorker extends Worker {

    private static final String USAGE_DATA_WORKER_ID = "DeChain_usage_data_worker";

    /**
     * 計測開始ミリ秒。
     */
    private static long startMillis;

    public UsageDataWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        if(!isAllowedAppUsageStats()) {
            NotificationProvider.sendNotification(
                    NotificationProvider.NotificationTitle.ON_BACKGROUND_ERROR_OCCURRED,
                    R.string.notification_message_background_noPermission
            );
            return Result.failure();
        }
        if(InternalRegisteredAccountManager.get() == null) {
            InternalBackgroundErrorReportManager.register(
                    new NotFoundInternalAccountException("Not found a register account for registering a background error report.")
            );
            NotificationProvider.sendNotification(
                    NotificationProvider.NotificationTitle.ON_BACKGROUND_ERROR_OCCURRED,
                    R.string.notification_message_background_error
            );
            return Result.failure();
        }

        UsageStatsManager usageManager = (UsageStatsManager)KozuZen.getInstance().getSystemService(Context.USAGE_STATS_SERVICE);
        PackageManager pm = KozuZen.getInstance().getPackageManager();

        //DeChain Usage Datas
        DailyUsageDatas todayDatas = DailyUsageDatas.create(Calendar.getInstance().get(Calendar.DAY_OF_MONTH));

        //Android Usage Datas
        List<UsageStats> stats = usageManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startMillis, System.currentTimeMillis());

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

        SendUsageDataRequest request = new SendUsageDataRequest(new SendUsageDataArguments(InternalRegisteredAccountManager.get().getMailAddress(), todayDatas));

        //DataBaseに送信
        PostAccessCallBack callBack = new PostAccessCallBack(request) {
            @Override
            public void onSuccess() {}

            @Override
            public void onFailure(@Nullable DataBasePostResponse response) {}

            @Override
            public void onTimeOut(DataBasePostResponse response) {}
        };

        DataBaseAccessor.sendPostRequest(request, callBack);
        //internalに保存
        try {
            InternalUsageDataManager.addDailyDatas(todayDatas);
        } catch (IOException e) {
            KozuZen.createErrorReport(e);
        }
        return Result.success();
    }

    private boolean isAllowedAppUsageStats() {
        AppOpsManager aoManager = (AppOpsManager)KozuZen.getInstance().getSystemService(Context.APP_OPS_SERVICE);
        int mode = aoManager.unsafeCheckOp(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), KozuZen.getInstance().getPackageName());
        if(mode != AppOpsManager.MODE_DEFAULT) return true;
        return KozuZen.getInstance().checkPermission("android.permission.PACKAGE_USAGE_STATS", android.os.Process.myPid(), Process.myUid())
                == PackageManager.PERMISSION_GRANTED;
    }

    public static void enqueueToWorkManager(Context context) {
        WorkManager wm = WorkManager.getInstance(context);
        //UsageDataWorkerがキューに登録されていない場合
        if(!isEnqueued(context)) {
            //再予約
            PeriodicWorkRequest req = new PeriodicWorkRequest.Builder(UsageDataWorker.class, Duration.ofDays(1L)).build();
            wm.enqueueUniquePeriodicWork(UsageDataWorker.USAGE_DATA_WORKER_ID, ExistingPeriodicWorkPolicy.KEEP, req);
            startMillis = System.currentTimeMillis();
        }
    }

    public static void removeFromQueue(Context context) {
        if(isEnqueued(context)) {
            WorkManager wm = WorkManager.getInstance(context);
            try {
                wm.cancelAllWorkByTag(UsageDataWorker.USAGE_DATA_WORKER_ID);
            } catch(Exception e) {
                KozuZen.createErrorReport(context, e);
            }
        }
    }

    public static boolean isEnqueued(Context context) {
        WorkManager wm = WorkManager.getInstance(context);
        try {
            return !wm.getWorkInfosByTag(UsageDataWorker.USAGE_DATA_WORKER_ID).get().isEmpty();
        } catch(Exception e) {
            KozuZen.createErrorReport(context, e);
        }
        return false;
    }
}
