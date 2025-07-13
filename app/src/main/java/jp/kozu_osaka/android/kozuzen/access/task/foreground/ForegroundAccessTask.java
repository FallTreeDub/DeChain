package jp.kozu_osaka.android.kozuzen.access.task.foreground;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.CallSuper;
import androidx.annotation.IdRes;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import jp.kozu_osaka.android.kozuzen.KozuZen;
import jp.kozu_osaka.android.kozuzen.R;
import jp.kozu_osaka.android.kozuzen.access.AccessResult;
import jp.kozu_osaka.android.kozuzen.access.LoadingFragment;
import jp.kozu_osaka.android.kozuzen.access.task.AccessTask;
import jp.kozu_osaka.android.kozuzen.annotation.InterruptibleMethod;
import jp.kozu_osaka.android.kozuzen.annotation.RunOnSubMethod;
import jp.kozu_osaka.android.kozuzen.notification.NotificationProvider;

/**
 * アプリがフォアグラウンド上にあり、アクセス処理の際にUI Threadへの画面遷移などの処理が必要な時に使用する{@link AccessTask}。
 * アクセス結果に応じた別Activityへの画面遷移などの、UI Threadへの操作も行う。
 *
 * <p>
 *  *     {@code ForegroundAccessTask}のサブクラスでUI Threadへの操作を行うには、{@link AccessTask#runOnUiThread(Runnable)}
 *  *     を使用する必要がある。
 *  * </p>
 */
public abstract class ForegroundAccessTask extends AccessTask {

    private final LoadingFragment loadingFragment;
    protected final FragmentActivity activity;
    @IdRes
    protected final int fragmentFrameId;

    /**
     *
     * @param activity SpreadSheet操作の際に表示させるロード画面を乗っける元画面。
     */
    public ForegroundAccessTask(FragmentActivity activity, @IdRes int fragmentFrameId) {
        this.loadingFragment = new LoadingFragment();
        this.activity = activity;
        this.fragmentFrameId = fragmentFrameId;
    }

    /**
     * ロード画面を表示する。
     * アクセス処理開始時に呼び出されなければならない。
     * すでにローディング画面が表示されている場合は無視される。
     */
    protected void showLoadingFragment() {
        runOnUiThread(() -> {
            FragmentManager manager = this.activity.getSupportFragmentManager();
            if(manager.findFragmentByTag(LoadingFragment.LOADING_FRAGMENT_TAG) == null) {
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.add(this.fragmentFrameId, this.loadingFragment, LoadingFragment.LOADING_FRAGMENT_TAG).commit();
            }
        });
    }

    /**
     * ロード画面を消す。
     * アクセス処理後の画面遷移後に呼び出されなければならない。
     * フラグメントがすでに消去されている場合は無視される。
     */
    private void removeLoadingFragment() {
        runOnUiThread(() -> {
            FragmentManager manager =  this.activity.getSupportFragmentManager();
            if(manager.findFragmentByTag(LoadingFragment.LOADING_FRAGMENT_TAG) != null) {
                manager.beginTransaction().remove(this.loadingFragment).commit();
            }
        });
    }

    /**
     * {@inheritDoc}
     * また、アクセス処理終了後、UIスレッドに対して、結果に応じた画面遷移をおこなう。
     * @exception IOException {@inheritDoc}
     * @exception ExecutionException {@inheritDoc}
     * @annotation {@link InterruptibleMethod}
     * @annotation {@link RunOnSubMethod}
     */
    @Override
    @RunOnSubMethod
    @InterruptibleMethod
    public abstract AccessResult run() throws ExecutionException, IOException;

    /**
     * @annotation {@link CallSuper}
     */
    @Override
    @CallSuper
    public void whenTimeOut() {
        NotificationProvider.sendNotification(
                NotificationProvider.NotificationTitle.ON_ACCESS_TIMEOUT,
                KozuZen.getInstance().getString(R.string.notification_message_access_timeout)
        );
        removeLoadingFragment();
    }

    @Override
    @CallSuper
    public void whenSuccess() {
        NotificationProvider.sendNotification(
                NotificationProvider.NotificationTitle.ON_ACCESS_SUCCEEDED,
                getSuccessNotificationMessage()
        );
        removeLoadingFragment();
    }

    @Override
    @CallSuper
    public void whenFailed(AccessResult.Failure failureResult) {
        NotificationProvider.sendNotification(
                NotificationProvider.NotificationTitle.ON_ACCESS_FAILED,
                failureResult.getMessage()
        );
        removeLoadingFragment();
    }

    @Override
    @CallSuper
    public void whenError(Exception e) {
        runOnUiThread(() -> {
            KozuZen.createErrorReport(this.activity, e);
        });
        removeLoadingFragment();
    }

    public abstract String getSuccessNotificationMessage();

    public abstract String getFailureNotificationMessage();

    /**
     * <p>
     *     UI Thread上で特定の動作を実行する。{@code ForegroundAccessTask}のサブクラスではアクセス結果を受けての画面遷移に使用される。
     * </p>
     * <p>
     *     内部的にはUI ThreadのLooperをもとにHandlerをインスタンス化し、
     *     {@link Handler#post(Runnable)}によって引数{@code runnable}の処理を実行している。
     * </p>
     *
     * @param runnable 実行するrunnable。
     */
    protected void runOnUiThread(Runnable runnable) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(runnable);
    }
}
