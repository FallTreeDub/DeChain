package jp.kozu_osaka.android.kozuzen;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.PrintWriter;
import java.io.StringWriter;

import jp.kozu_osaka.android.kozuzen.internal.InternalBackgroundErrorReportManager;
import jp.kozu_osaka.android.kozuzen.util.NotificationProvider;

/**
 * このアプリケーションの実体。
 */
public final class KozuZen extends Application {

    private static KozuZen instance;
    private static final String[] BUG_REPORT_HEADER = new String[] {
            "------------------",
            "DECHAIN BUG REPORT",
            "------------------",
            "Manufacturer=" + Build.MANUFACTURER,
            "Device=" + Build.DEVICE,
            "Model=" + Build.MODEL,
            "Product=" + Build.PRODUCT,
            "hardware=" + Build.HARDWARE,
            "Identifier ID=" + Build.ID,
            "Android API=" + Build.VERSION.SDK_INT,
            "DeChain Version Code=" + BuildConfig.VERSION_CODE,
            "------------------",
            ""
    };

    private static Class<? extends Activity> currentActivity = null;
    private static Class<? extends Activity> lastOpenedActivity = null;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        NotificationProvider.initNotificationChannel();
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {

            @Override
            public void onActivityPaused(@NonNull Activity activity) {
                currentActivity = null;
            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {
                currentActivity = activity.getClass();
                lastOpenedActivity = activity.getClass();
            }

            @Override
            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {}
            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {}
            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {}
            @Override
            public void onActivityStarted(@NonNull Activity activity) {}
            @Override
            public void onActivityStopped(@NonNull Activity activity) {}
        });
    }

    /**
     * 今一番上にstackされているActivityのclassを取得する。
     * アプリがバックグラウンドの場合はnull。
     * @return
     */
    public static Class<? extends Activity> getCurrentActivity() {
        return currentActivity;
    }

    public static Class<? extends Activity> getLastOpenedActivity() {
        return lastOpenedActivity;
    }

    /**
     * エラー発生時の開発者へのエラーレポートを作成し、エラー内容を画面に表示させる。
     * @param context エラー画面への遷移元のcontext。
     * @param e アプリ走行時に発動したエラー
     */
    public static void createErrorReport(Context context, Exception e) {
        Intent reportIntent = new Intent(context, ReportActivity.class);
        reportIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        reportIntent.putExtra(Constants.IntentExtraKey.REPORT_BODY, KozuZen.generateReport(e));
        context.startActivity(reportIntent);
    }

    /**
     * バックグラウンドで例外が発生した場合に、開発者へのエラーレポートを作成し、
     * json方式で内部ストレージに保存する。
     * @param e バックグラウンド時に発動したエラー
     */
    public static void createErrorReport(Exception e) {
        InternalBackgroundErrorReportManager.register(e);
        NotificationProvider.sendNotification(
                NotificationProvider.NotificationTitle.ON_BACKGROUND_ERROR_OCCURRED,
                NotificationProvider.NotificationIcon.NONE,
                R.string.notification_message_background_error
        );
    }

    /**
     * エラー発生時のレポートを例外から作成する。
     * @param e 発生した例外。
     * @return 作成されたレポート本体。
     */
    public static String generateReport(Exception e) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        for(String l : BUG_REPORT_HEADER) {
            printWriter.append(l).append("\n");
        }
        e.printStackTrace(printWriter);
        return stringWriter.toString();
    }

    /**
     * このアプリケーションの実体を取得する。
     * @return アプリケーションの実体
     */
    public static KozuZen getInstance() {
        return instance;
    }
}
