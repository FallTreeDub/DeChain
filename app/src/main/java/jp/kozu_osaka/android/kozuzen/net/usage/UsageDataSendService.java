package jp.kozu_osaka.android.kozuzen.net.usage;

import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.IBinder;

import androidx.annotation.ArrayRes;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import jp.kozu_osaka.android.kozuzen.ExperimentType;
import jp.kozu_osaka.android.kozuzen.KozuZen;
import jp.kozu_osaka.android.kozuzen.R;
import jp.kozu_osaka.android.kozuzen.exception.GetAccessException;
import jp.kozu_osaka.android.kozuzen.internal.InternalRegisteredAccountManager;
import jp.kozu_osaka.android.kozuzen.internal.InternalUsageDataManager;
import jp.kozu_osaka.android.kozuzen.net.DataBaseAccessor;
import jp.kozu_osaka.android.kozuzen.net.DataBaseGetResponse;
import jp.kozu_osaka.android.kozuzen.net.DataBasePostResponse;
import jp.kozu_osaka.android.kozuzen.net.argument.get.GetAverageOfUsageOneDayArguments;
import jp.kozu_osaka.android.kozuzen.net.argument.post.SendUsageDataArguments;
import jp.kozu_osaka.android.kozuzen.net.request.Request;
import jp.kozu_osaka.android.kozuzen.net.request.get.GetAverageOfUsageOneDayRequest;
import jp.kozu_osaka.android.kozuzen.net.request.post.SendUsageDataRequest;
import jp.kozu_osaka.android.kozuzen.net.usage.data.DailyUsageDatas;
import jp.kozu_osaka.android.kozuzen.net.usage.data.UsageData;
import jp.kozu_osaka.android.kozuzen.security.Secrets;
import jp.kozu_osaka.android.kozuzen.util.Logger;
import jp.kozu_osaka.android.kozuzen.util.NotificationProvider;

public final class UsageDataSendService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startID) {

        //実験期間外の場合
        if(!isTodayInExperimentDuration()) {
            stopSelf();
            return START_NOT_STICKY;
        }

        startForeground(startID, NotificationProvider.buildNotification(
                this,
                NotificationProvider.NotificationIcon.NONE,
                getString(R.string.notification_title_sendUsage),
                null
        ));

        final boolean needNotification; //通知を送る必要があるか。
        final ExperimentType type = InternalRegisteredAccountManager.getExperimentType();
        needNotification = !(isInExperimentNonNotificationDuration() || type.equals(ExperimentType.TYPE_NON_NOTIFICATION));

        DailyUsageDatas todayData;
        //今日のデータ準備
        todayData = createTodayData();

        DataBasePostResponse response;
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            SendUsageDataRequest req = new SendUsageDataRequest(
                    new SendUsageDataArguments(InternalRegisteredAccountManager.getMailAddress(), todayData)
            );
            Future<DataBasePostResponse> future = executor.submit(() -> DataBaseAccessor.sendPostRequestSynchronous(req));
            response = future.get();
        } catch(ExecutionException e) {
            KozuZen.createErrorReport(e);
            stopForeground(true);
            return START_NOT_STICKY;
        } catch(InterruptedException e) {
            stopForeground(true);
            return START_NOT_STICKY;
        }
        @StringRes Integer msgID = null;
        if(response != null) {
            switch(response.getResponseCode()) {
                case Request.RESPONSE_CODE_ARGUMENT_NULL:
                    msgID = R.string.error_argNull;
                    break;
                case Request.RESPONSE_CODE_ARGUMENT_NON_SIGNATURES:
                    msgID = R.string.error_notFoundSignatures;
                    break;
                case SendUsageDataRequest.ERROR_CODE_FAILURE_REGISTER:
                    msgID = R.string.error_errorResponse_sendUsage_failedReg;
                    break;
            }
            if(msgID != null) {
                KozuZen.createErrorReport(new GetAccessException(msgID));
            }
        } else {
            KozuZen.createErrorReport(new GetAccessException(R.string.error_unknown));
        }

        //デバイス内部に保存
        try {
            Logger.i("addDailyDatas");
            InternalUsageDataManager.addDailyDatas(todayData);
        } catch(IOException e) {
            KozuZen.createErrorReport(e);
            stopForeground(true);
            return START_NOT_STICKY;
        }

        //通知が不必要ならここで終了
        if(!needNotification) {
            Logger.i("Not need notification, end");
            stopForeground(true);
            return START_NOT_STICKY;
        }
        if(type.isCompareWithSelf()) {
            Logger.i("compare with self");
            onCompareWithSelf(todayData, type);
        } else {
            try {
                Logger.i("compare with others");
                onCompareWithOthers(todayData, type);
            } catch(IOException | GetAccessException e) {
                KozuZen.createErrorReport(e);
            }
        }
        stopForeground(true);
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void onCompareWithSelf(@NotNull DailyUsageDatas todayData, @NotNull ExperimentType type) {
        DailyUsageDatas yesterdayDatas;
        try {
            yesterdayDatas = InternalUsageDataManager.getDataOf(Calendar.getInstance(Locale.JAPAN).get(Calendar.DAY_OF_MONTH) - 1);
            //実験1日目は前日の使用時間データがInternalにたまっていないので前日との比較は不可
            if(yesterdayDatas == null) {
                Logger.i("no send because of null");
                return;
            }
            long millisSubtracted = todayData.getUsageTimeInMillis() - yesterdayDatas.getUsageTimeInMillis();
            Logger.i("send noti");
            sendNotification(millisSubtracted < 0, type, Math.abs(millisSubtracted));
        } catch(IOException e) {
            KozuZen.createErrorReport(e);
        }
    }

    private void onCompareWithOthers(@NotNull DailyUsageDatas todayData, @NotNull ExperimentType type) throws IOException, GetAccessException {
        Calendar today = Calendar.getInstance(Locale.JAPAN);
        GetAverageOfUsageOneDayRequest getAveRequest = new GetAverageOfUsageOneDayRequest(
                new GetAverageOfUsageOneDayArguments(today.get(Calendar.YEAR), today.get(Calendar.MONTH) + 1, today.get(Calendar.DAY_OF_MONTH))
        );

        DataBaseGetResponse response;
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future<DataBaseGetResponse> future = executor.submit(() -> DataBaseAccessor.sendGetRequestSynchronous(getAveRequest));
            response = future.get();
        } catch(ExecutionException e) {
            KozuZen.createErrorReport(e);
            executor.shutdownNow();
            return;
        } catch(InterruptedException e) {
            executor.shutdownNow();
            return;
        }
        if(response == null) {
            throw new GetAccessException(R.string.error_errorResponse_sendUsage_averageFromDBIsNull);
        } else {
            @StringRes Integer msgID;
            switch(response.getResponseCode()) {
                case Request.RESPONSE_CODE_ARGUMENT_NULL: msgID = R.string.error_argNull;
                    break;
                case Request.RESPONSE_CODE_ARGUMENT_NON_SIGNATURES: msgID = R.string.error_notFoundSignatures;
                    break;
                case GetAverageOfUsageOneDayRequest.ERROR_CODE_NOT_FOUND_TIMESTAMP: msgID = R.string.error_errorResponse_getAverage_notFoundTimeStamp;
                    break;
                case GetAverageOfUsageOneDayRequest.ERROR_CODE_NOT_FOUND_START_ROW: msgID = R.string.error_errorResponse_getAverage_notFoundStartRowNum;
                    break;
                case GetAverageOfUsageOneDayRequest.ERROR_CODE_NOT_FOUND_TOTALUSAGE: msgID = R.string.error_errorResponse_getAverage_notFoundtotalUsage;
                    break;
                case Request.RESPONSE_CODE_NO_ERROR:
                case Request.RESPONSE_CODE_NO_ERROR_WITH_MESSAGE:
                    msgID = null;
                    break;
                default: msgID = R.string.error_unknown;
                    break;
            }
            if(msgID != null) throw new GetAccessException(msgID);
        }

        int average = getAveRequest.resultParse(response.getResultJsonElement());
        long timeMillisSubtracted = todayData.getUsageTimeInMillis() - TimeUnit.MINUTES.toMillis(average);
        sendNotification(timeMillisSubtracted < 0, type, Math.abs(timeMillisSubtracted));
    }

    private boolean isTodayInExperimentDuration() {
        Calendar today = Calendar.getInstance(Locale.JAPAN);
        return today.compareTo(Secrets.EXPERIMENT_START_DAY) >= 0 && today.compareTo(Secrets.EXPERIMENT_END_DAY) <= 0;
    }

    private boolean isInExperimentNonNotificationDuration() {
        Calendar today = Calendar.getInstance(Locale.JAPAN);
        return today.compareTo(Secrets.EXPERIMENT_START_DAY) >= 0 && today.compareTo(Secrets.EXPERIMENT_NON_NOTIFICATION_END_DAY) <= 0;
    }

    private DailyUsageDatas createTodayData() {
        UsageStatsManager usageManager = (UsageStatsManager)KozuZen.getInstance().getSystemService(Context.USAGE_STATS_SERVICE);
        PackageManager pm = KozuZen.getInstance().getPackageManager();

        //DeChain Usage Datas
        DailyUsageDatas todayDatas = DailyUsageDatas.create(Calendar.getInstance().get(Calendar.DAY_OF_MONTH));

        //Android Usage Datas
        //前日の20時から今日の19時59分59秒までの利用時間
        Calendar today = Calendar.getInstance(Locale.JAPAN);
        Calendar todayEightPM = new Calendar.Builder()
                .setDate(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH))
                .setTimeOfDay(19, 59, 59)
                .build();
        Calendar yesterdayEightPM = (Calendar)todayEightPM.clone();
        yesterdayEightPM.add(Calendar.DAY_OF_MONTH, -1);
        yesterdayEightPM.add(Calendar.SECOND, 1);
        List<UsageStats> stats = usageManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                yesterdayEightPM.getTimeInMillis(),
                todayEightPM.getTimeInMillis()
        );

        for(UsageStats stat : stats) {
            if(stat.getTotalTimeInForeground() == 0L) {
                continue;
            }

            ApplicationInfo info;
            try {
                info = pm.getApplicationInfo(stat.getPackageName(), 0);
            } catch(PackageManager.NameNotFoundException e) {
                continue;
            }
            String appName = pm.getApplicationLabel(info).toString();
            //json内のStringに格納できない文字をシーケンス
            appName = appName.replace("\\", "\\\\");
            appName = appName.replace("\"", "\\\"");
            appName = appName.replace("'", "\\'");

            UsageData data;
            if(info.category == ApplicationInfo.CATEGORY_SOCIAL) {
                data = new UsageData(UsageData.AppType.SNS, appName, stat.getTotalTimeInForeground());
            } else if(info.category == ApplicationInfo.CATEGORY_GAME) {
                data = new UsageData(UsageData.AppType.GAMES, appName, stat.getTotalTimeInForeground());
            } else {
                continue;
            }
            todayDatas.add(data);
        }
        return todayDatas;
    }

    private void sendNotification(boolean isSuperior, ExperimentType type, long millisSubtracted) {

        final NotificationProvider.NotificationTitle title;
        final @ArrayRes int messageArrayID;

        if(type.isCompareWithSelf()) {
            title = isSuperior ?
                    NotificationProvider.NotificationTitle.DAILY_COMPARE_WITH_SELF_SUPERIOR :
                    NotificationProvider.NotificationTitle.DAILY_COMPARE_WITH_SELF_INFERIOR;
        } else {
            title = isSuperior ?
                    NotificationProvider.NotificationTitle.DAILY_COMPARE_WITH_OTHER_SUPERIOR :
                    NotificationProvider.NotificationTitle.DAILY_COMPARE_WITH_OTHER_INFERIOR;
        }

        if(type.isPositive()) {
            messageArrayID = isSuperior ?
                    R.array.notification_message_daily_positive_superior :
                    R.array.notification_message_daily_positive_inferior;
        } else {
            messageArrayID = isSuperior ?
                    R.array.notification_message_daily_negative_superior :
                    R.array.notification_message_daily_negative_inferior;
        }

        String[] messages = getResources().getStringArray(messageArrayID);
        String message = messages[new Random().nextInt(messages.length)];
        long wholeMinutes = TimeUnit.MILLISECONDS.toMinutes(millisSubtracted);
        int hour = (int)(wholeMinutes / 60);
        int minute = (int)(wholeMinutes - hour * 60);

        NotificationProvider.sendNotification(String.format(
                        Locale.JAPAN,
                        title.getTitle(),
                        hour, minute),
                NotificationProvider.NotificationIcon.DECHAIN_DUCK, message);
    }
}
