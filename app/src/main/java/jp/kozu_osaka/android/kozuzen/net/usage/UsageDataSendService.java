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
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
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
import jp.kozu_osaka.android.kozuzen.net.argument.get.GetAverageOfUsageOneDayArguments;
import jp.kozu_osaka.android.kozuzen.net.callback.GetAccessCallBack;
import jp.kozu_osaka.android.kozuzen.net.request.Request;
import jp.kozu_osaka.android.kozuzen.net.request.get.GetAverageOfUsageOneDayRequest;
import jp.kozu_osaka.android.kozuzen.net.usage.data.DailyUsageDatas;
import jp.kozu_osaka.android.kozuzen.net.usage.data.UsageData;
import jp.kozu_osaka.android.kozuzen.security.Secrets;
import jp.kozu_osaka.android.kozuzen.util.NotificationProvider;

public final class UsageDataSendService extends Service {

    private final ExecutorService executorService = Executors.newFixedThreadPool(2);

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
        final boolean needAverage; //他人のデータの平均値を求める必要があるか。
        final ExperimentType type = InternalRegisteredAccountManager.getExperimentType();
        needNotification = !(isInExperimentNonNotificationDuration() || type.equals(ExperimentType.TYPE_NON_NOTIFICATION));
        needAverage = !type.isCompareWithSelf();

        Integer average = null;
        try {
            Future<Void> registerUsageDataFuture = executorService.submit(new RegisterTodaysUsageCallable());
            registerUsageDataFuture.get();

            if(needAverage) {
                Future<Integer> averageFuture = executorService.submit(new GetAverageCallable());
                average = averageFuture.get();
            }
        } catch(InterruptedException e) { //強制終了時
            stopForeground(true);
            return START_NOT_STICKY;
        } catch (ExecutionException e) {
            KozuZen.createErrorReport(e);
            stopForeground(true);
            return START_NOT_STICKY;
        }

        //他人のデータの平均値が必要なのにnullの場合は例外を出す
        if(average == null) {
            if(needAverage) {
                KozuZen.createErrorReport(new RuntimeException("The usage average from database is null."));
                stopForeground(true);
                return START_NOT_STICKY;
            }
        }

        //通知が不必要ならここで終了
        if(!needNotification) {
            stopForeground(true);
            return START_NOT_STICKY;
        }

        //比較するために、今日の分のデータを準備
        boolean isSuperior;
        Calendar today = Calendar.getInstance();
        DailyUsageDatas todayDatas;
        try {
            todayDatas = InternalUsageDataManager.getDataOf(today.get(Calendar.DAY_OF_MONTH));
            if(todayDatas == null) {
                KozuZen.createErrorReport(new IllegalStateException("today's data is null."));
                stopForeground(true);
                return START_NOT_STICKY;
            }
        } catch(IOException e) {
            KozuZen.createErrorReport(e);
            stopForeground(true);
            return START_NOT_STICKY;
        }

        //今日のデータの値との比較対象はtypeごとに異なる
        final long millisSubtracted;
        if(type.isCompareWithSelf()) {
            DailyUsageDatas yesterdayDatas;
            try {
                yesterdayDatas = InternalUsageDataManager.getDataOf(today.get(Calendar.DAY_OF_MONTH) - 1);
                //実験1日目は前日の使用時間データがInternalにたまっていないので前日との比較は不可
                if(yesterdayDatas == null) {
                    stopForeground(true);
                    return START_NOT_STICKY;
                }
                millisSubtracted = todayDatas.getUsageTimeInMillis() - yesterdayDatas.getUsageTimeInMillis();
                isSuperior = millisSubtracted < 0;
            } catch(IOException e) {
                KozuZen.createErrorReport(e);
                stopForeground(true);
                return START_NOT_STICKY;
            }
        } else {
            millisSubtracted = todayDatas.getUsageTimeInMillis() - TimeUnit.MINUTES.toMillis(average);
            isSuperior = millisSubtracted < 0;
        }

        sendNotification(isSuperior, type, millisSubtracted);
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }

    private boolean isTodayInExperimentDuration() {
        Calendar today = Calendar.getInstance();
        return today.compareTo(Secrets.EXPERIMENT_START_DAY) >= 0 && today.compareTo(Secrets.EXPERIMENT_END_DAY) <= 0;
    }

    private boolean isInExperimentNonNotificationDuration() {
        Calendar today = Calendar.getInstance();
        return today.compareTo(Secrets.EXPERIMENT_START_DAY) >= 0 && today.compareTo(Secrets.EXPERIMENT_NON_NOTIFICATION_END_DAY) <= 0;
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

        millisSubtracted = Math.abs(millisSubtracted);
        NotificationProvider.sendNotification(String.format(
                        Locale.JAPAN,
                        title.getTitle(),
                        TimeUnit.MILLISECONDS.toHours(millisSubtracted),
                        TimeUnit.MILLISECONDS.toMinutes(millisSubtracted)),
                NotificationProvider.NotificationIcon.DECHAIN_DUCK, message);
    }

    private static final class GetAverageCallable implements Callable<Integer> {

        public GetAverageCallable() {}

        @Override
        @Nullable
        public Integer call() {
            final Integer[] result = {null};
            try {
                final CountDownLatch latch = new CountDownLatch(1);

                Calendar today = Calendar.getInstance();
                GetAverageOfUsageOneDayRequest getAveRequest = new GetAverageOfUsageOneDayRequest(
                        new GetAverageOfUsageOneDayArguments(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH))
                );

                GetAccessCallBack<Integer> getAccessCallBack = new GetAccessCallBack<>(getAveRequest) {
                    @Override
                    public void onSuccess(@NotNull DataBaseGetResponse response) {
                        result[0] = this.getRequest.resultParse(response.getResultJsonElement());
                        latch.countDown();
                    }

                    @Override
                    public void onFailure(@Nullable DataBaseGetResponse response) {
                        @StringRes Integer msgID = null;
                        if(response != null) {
                            switch(response.getResponseCode()) {
                                case Request.RESPONSE_CODE_ARGUMENT_NULL:
                                    msgID = R.string.error_argNull;
                                    break;
                                case Request.RESPONSE_CODE_ARGUMENT_NON_SIGNATURES:
                                    msgID = R.string.error_notFoundSignatures;
                                    break;
                                case GetAverageOfUsageOneDayRequest.ERROR_CODE_NOT_FOUND_TIMESTAMP:
                                    msgID = R.string.error_errorResponse_getAverage_notFoundTimeStamp;
                                    break;
                                case GetAverageOfUsageOneDayRequest.ERROR_CODE_NOT_FOUND_START_ROW:
                                    msgID = R.string.error_errorResponse_getAverage_notFoundStartRowNum;
                                    break;
                                case GetAverageOfUsageOneDayRequest.ERROR_CODE_NOT_FOUND_TOTALUSAGE:
                                    msgID = R.string.error_errorResponse_getAverage_notFoundtotalUsage;
                                    break;
                            }
                        }
                        if(msgID == null) msgID = R.string.error_unknown;

                        KozuZen.createErrorReport(new GetAccessException(msgID));
                        latch.countDown();
                    }

                    @Override
                    public void onTimeOut() {
                        retry();
                        latch.countDown();
                    }
                };
                DataBaseAccessor.sendGetRequest(getAveRequest, getAccessCallBack);
                latch.await();
            } catch(InterruptedException e) {
                return null;
            }
            return result[0];
        }
    }

    private static final class RegisterTodaysUsageCallable implements Callable<Void> {

        public RegisterTodaysUsageCallable() {}

        @Override
        public Void call() {
            UsageStatsManager usageManager = (UsageStatsManager)KozuZen.getInstance().getSystemService(Context.USAGE_STATS_SERVICE);
            PackageManager pm = KozuZen.getInstance().getPackageManager();

            //DeChain Usage Datas
            DailyUsageDatas todayDatas = DailyUsageDatas.create(Calendar.getInstance().get(Calendar.DAY_OF_MONTH));

            //Android Usage Datas
            //前日の20時から今日の19時59分59秒までの利用時間
            Calendar todayEightPM = new Calendar.Builder()
                    .setTimeOfDay(19, 59, 59)
                    .build();
            Calendar yesterdayEightPM = new Calendar.Builder()
                    .set(Calendar.DAY_OF_MONTH, todayEightPM.get(Calendar.DAY_OF_MONTH) - 1)
                    .setTimeOfDay(20, 0, 0)
                    .build();
            List<UsageStats> stats = usageManager.queryUsageStats(
                    UsageStatsManager.INTERVAL_DAILY,
                    yesterdayEightPM.getTimeInMillis(),
                    todayEightPM.getTimeInMillis()
            );

            try {
                for(UsageStats stat : stats) {
                    ApplicationInfo info = pm.getApplicationInfo(stat.getPackageName(), 0);
                    String appName = pm.getApplicationLabel(info).toString();
                    //json内のStringに格納できない文字をシーケンス
                    appName = appName.replace("\\", "\\\\");
                    appName = appName.replace("\"", "\\\"");
                    appName = appName.replace("'", "\\'");
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

            //デバイス内部に保存
            try {
                InternalUsageDataManager.addDailyDatas(todayDatas);
            } catch(IOException e) {
                KozuZen.createErrorReport(e);
            }
            return null;
        }
    }
}
