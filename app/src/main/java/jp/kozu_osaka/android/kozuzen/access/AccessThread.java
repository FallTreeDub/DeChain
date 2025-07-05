package jp.kozu_osaka.android.kozuzen.access;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.sheets.v4.Sheets;

import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import jp.kozu_osaka.android.kozuzen.Constants;
import jp.kozu_osaka.android.kozuzen.KozuZen;
import jp.kozu_osaka.android.kozuzen.R;
import jp.kozu_osaka.android.kozuzen.access.task.AccessTask;
import jp.kozu_osaka.android.kozuzen.notification.NotificationProvider;
import kotlin.Suppress;

/*
        //全体のタイムアウト
        Log.i(Constants.Debug.LOGNAME_INFO, "entire timeout set");
        this.ENTIRE_TIMEOUT_SERVICE.schedule(() -> {
            Log.i(Constants.Debug.LOGNAME_INFO, "entire timeout-ed.");
            AccessThread.this.task.whenTimeOut();
            AccessThread.this.interrupt();
        }, ENTIRE_TIMEOUT_SECONDS, TimeUnit.SECONDS);

        int nowAccessTry = 0;
        Log.i(Constants.Debug.LOGNAME_INFO, "enter while loop");
        while(!this.isInterrupted()) {
            int waitSec = new Random().nextInt((int)(ACCESS_WAIT_STEP_SECONDS * Math.pow(2, nowAccessTry - 1)) + 1) + 1;
            nowAccessTry++;
            try {
                Log.i(Constants.Debug.LOGNAME_INFO, "wait" + waitSec + "sec");
                Thread.sleep(TimeUnit.SECONDS.toMillis(waitSec));
            } catch(InterruptedException e) {
                Log.i(Constants.Debug.LOGNAME_INFO, "exponential_backoff was interrupted.");
            }

            //1回のアクセスあたりのタイムアウト設定
            Log.i(Constants.Debug.LOGNAME_INFO, "once timeout set");
            this.ONCE_ACCESS_TIMEOUT_SERVICE = Executors.newScheduledThreadPool(1);
            this.ONCE_ACCESS_TIMEOUT_SERVICE.schedule(() -> {
                Log.i(Constants.Debug.LOGNAME_INFO, "once access was timeout-ed.");
                this.TASK_EXECUTE_SERVICE.shutdownNow();
                this.ONCE_ACCESS_TIMEOUT_SERVICE.shutdownNow();
            }, ONCE_ACCESS_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            //アクセスservice
            Log.i(Constants.Debug.LOGNAME_INFO, "task execute set");
            this.TASK_EXECUTE_SERVICE = Executors.newFixedThreadPool(1);
            Future<AccessResult> taskFuture = this.TASK_EXECUTE_SERVICE.submit(AccessThread.this.task::run);
            try {
                Log.i(Constants.Debug.LOGNAME_INFO, "asd");
                AccessResult result = taskFuture.get();
                Log.i(Constants.Debug.LOGNAME_INFO, result.toString());
                if(result instanceof AccessResult.Success) {
                    this.task.whenSuccess();
                } else if(result instanceof AccessResult.Failure) {
                    this.task.whenFailed((AccessResult.Failure)result);
                }
                this.interrupt();
            } catch(ExecutionException e) {
                if(e.getCause() != null && e.getCause() instanceof GoogleJsonResponseException) {
                    GoogleJsonResponseException googleE = (GoogleJsonResponseException)e.getCause();
                    if(googleE.getStatusCode() == GOOGLE_JSON_EXCEPTION_API_ERROR_CODE) {
                        this.ONCE_ACCESS_TIMEOUT_SERVICE.shutdownNow();
                        this.TASK_EXECUTE_SERVICE.shutdownNow();
                    }
                } else {
                    this.task.whenError(e);
                    this.interrupt();
                    break;
                }
            } catch(InterruptedException e) {
                Log.i(Constants.Debug.LOGNAME_INFO, "task execute service was interrupted.");
            }
        }*/

/**
 * <p>
 *     UIスレッドから分離してSpreadSheetへのアクセスを行うためのワーカースレッドの実体。
 * </p>
 * <p>
 *     SpreadSheetへのアクセス内容は{@link AccessTask}によって確定され、コンストラクタで
 *     この{@code AccessThread}をインスタンス化する際に引数として渡す必要がある。
 * </p>
 * <p>
 *     アクセスは{@link AccessThread#start()}によって開始される。
 *     {@link AccessThread#ENTIRE_TIMEOUT_SECONDS}で規定されたタイムアウトの秒数になると、そのアクセスは{@link AccessThread#interrupt()}
 *     の自動実行によって強制終了される。
 * </p>
 * <p>
 *     SpreadSheet API v4を用いてのSpreadsheetへのアクセスを実装しているため、API利用制限でアクセスできないことがある。
 *     アクセス失敗時に適切なインターバルを空けて再アクセスするために、アクセス処理の中で指数バックオフアルゴリズム(Exponential Backoff)を用いる。
 * </p>
 * <p>
 *     アクセス後、{@link AccessTask#run(Sheets.Spreadsheets)}内で実装されている、アクセス結果ごとの画面遷移が行われ、
 *     同時に{@link AccessThread#run()}内で{@link NotificationProvider}を通してアクセス結果を通知として配信する。
 * </p>
 *
 * @see NotificationProvider#sendNotification(String)
 * @see NotificationProvider#sendErrorNotification(String)
 * @see AccessTask#run(Sheets.Spreadsheets)
 */
public class AccessThread extends Thread {
    private final AccessTask task;

    /*private final ScheduledExecutorService ENTIRE_TIMEOUT_SERVICE = Executors.newScheduledThreadPool(1);
    private ScheduledExecutorService ONCE_ACCESS_TIMEOUT_SERVICE = null;
    private ExecutorService TASK_EXECUTE_SERVICE = null;*/

    private final ExecutorService ENTIRE_SERVICE = Executors.newFixedThreadPool(1);
    private final AccessEntireRunnable ENTIRE_RUNNABLE = new AccessEntireRunnable();

    /**
     * {@code AccessThread}による一度のアクセスへのタイムアウト(単位は秒)。
     * この秒数を過ぎれば{@link AccessThread#interrupt()}によって強制終了される。
     */
    private static final int ENTIRE_TIMEOUT_SECONDS = 120;
    private static final int ONCE_ACCESS_TIMEOUT_SECONDS = 10;
    private static final int ACCESS_WAIT_STEP_SECONDS = 3;

    /**
     * {@link GoogleJsonResponseException}が、API利用制限によって発生した際のエラーコード。
     */
    private static final int GOOGLE_JSON_EXCEPTION_API_ERROR_CODE = 429;

    /**
     * @param task アクセス内容。
     */
    public AccessThread(AccessTask task) {
        this.task = task;
    }

    @Override
    public void run() {
        Future<?> entireFuture = this.ENTIRE_SERVICE.submit(ENTIRE_RUNNABLE);
        try {
            entireFuture.get(ENTIRE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch(TimeoutException timeEx) {
            task.whenTimeOut();
            this.interrupt();
        } catch(InterruptedException inEx) { //this.interrupt();によるAccessThreadのinterruptで発火
            ENTIRE_RUNNABLE.interrupt();
        } catch(ExecutionException exEx) {
            task.whenError(exEx); //createErrorReportをする
        }
    }

    /**
     * この{@code AccessThread}を強制終了する。{@link Thread#interrupt()}と内部処理は同様。
     *
     * @see Thread#interrupt()
     */
    @Override
    public void interrupt() {
        super.interrupt();
    }

    private class AccessEntireRunnable implements Runnable {

        private int nowAccessTry = 1;
        private ExecutorService taskExecuteService;

        @Override
        public void run() {
            while(true) {
                //backoff遅延
                try {
                    int waitSec = new Random().nextInt((int)(ACCESS_WAIT_STEP_SECONDS * Math.pow(2, nowAccessTry - 1)) + 1) + 1;
                    nowAccessTry++;
                    Thread.sleep(TimeUnit.SECONDS.toMillis(waitSec));
                } catch(InterruptedException interruptEx) {
                    //Thread.sleep()の途中でAccessThreadがinterruptされたとき。
                    //Thread.sleepの状態は解除される。
                    break;
                }

                taskExecuteService = Executors.newFixedThreadPool(1);
                Future<AccessResult> executeFuture = taskExecuteService.submit(task::run);
                try {
                    AccessResult result = executeFuture.get(ONCE_ACCESS_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                    if(result instanceof AccessResult.Success) {
                        task.whenSuccess();
                    } else if(result instanceof AccessResult.Failure) {
                        task.whenFailed((AccessResult.Failure)result);
                    }
                    break;
                } catch(InterruptedException interruptEx) {
                    //アクセス処理途中でAccessThreadがinterruptされたとき
                    this.interrupt();
                    break;
                } catch(TimeoutException timeoutEx) {
                    continue; //タイムアウト時は再試行(whileをcontinueする)
                } catch(ExecutionException exeEx) { //SpreadSheetアクセス中に例外が発生した時
                    if(exeEx.getCause() != null && exeEx.getCause() instanceof GoogleJsonResponseException) {
                        GoogleJsonResponseException googleEx = (GoogleJsonResponseException)exeEx.getCause();
                        if(googleEx.getStatusCode() == GOOGLE_JSON_EXCEPTION_API_ERROR_CODE) {
                            continue;
                        }
                    }
                    task.whenError(exeEx);
                    AccessThread.this.interrupt();
                    break;
                }
            }
        }

        /**
         * {@code AccessThread}から{@code AccessEntireRunnable}の実行を
         * interruptする。
         */
        public void interrupt() {
            this.taskExecuteService.shutdownNow();
        }
    }
}
