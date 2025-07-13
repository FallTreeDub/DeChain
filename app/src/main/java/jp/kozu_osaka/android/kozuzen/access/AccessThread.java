package jp.kozu_osaka.android.kozuzen.access;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;

import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import jp.kozu_osaka.android.kozuzen.access.task.AccessTask;

/**
 * <p>
 *     UIスレッドから分離してSpreadSheetへのアクセスを行うためのスレッドの実体。
 * </p>
 * <p>
 *     SpreadSheetへのアクセス内容は{@link AccessTask}によって確定され、コンストラクタで
 *     この{@code AccessThread}をインスタンス化する際に引数として渡す必要がある。
 *     <blockquote><pre>
 *         //使用例
 *         AccessThread accessThread = new AccessThread(
 *                 new TentativeRegisterTask(
 *                         context, R.id.fragmentFrame,
 *                         mailAddress, password, signupQuestion)
 *         );
 *     </pre></blockquote>
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
 *     アクセス後、そのアクセス結果によって{@link AccessTask#whenSuccess()}、{@link AccessTask#whenFailed(AccessResult.Failure)}、{@link AccessTask#whenTimeOut()}
 *     が実行される。
 * </p>
 */
public final class AccessThread extends Thread {
    private final AccessTask task;

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
        } catch(InterruptedException inEx) { //this.interrupt();によるAccessThreadのinterruptで発火
        } catch(ExecutionException exEx) {
            task.whenError(exEx); //createErrorReportをする
        } finally {
            this.interrupt();
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
        this.ENTIRE_SERVICE.shutdownNow();
    }

    private class AccessEntireRunnable implements Runnable {

        private int nowAccessTry = 1;
        private final ExecutorService taskExecuteService = Executors.newFixedThreadPool(1);

        @Override
        public void run() {
            while(true) {
                //backoff遅延
                try {
                    int waitSec = new Random().nextInt((int)(ACCESS_WAIT_STEP_SECONDS * Math.pow(2, nowAccessTry - 1)) + 1) + 1;
                    nowAccessTry++;
                    Thread.sleep(TimeUnit.SECONDS.toMillis(waitSec));
                } catch(InterruptedException interruptEx) {
                    //Thread.sleep()の途中でAccessEntireRunnableが動いているServiceがinterruptされたとき。
                    //Thread.sleepの状態は解除される。
                    break;
                }

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
                    //アクセス処理途中でAccessEntireRunnableが動いているServiceがinterruptされたとき
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
                } finally {
                    this.interrupt();
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
