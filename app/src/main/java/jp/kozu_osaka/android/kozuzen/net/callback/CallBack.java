package jp.kozu_osaka.android.kozuzen.net.callback;

public abstract class CallBack {

    protected static final int DEFAULT_MAXIMUM_RETRY = 3;

    /**
     * 現在のリトライ回数
     */
    protected int nowRetry;

    /**
     * 所定のリクエストを再度実行する。タイムアウト時などに使用する。
     * @param maximumRetry 最大リトライ回数
     */
    protected abstract void retry(int maximumRetry);

    protected final void retry() {
        this.retry(DEFAULT_MAXIMUM_RETRY);
    }
}
