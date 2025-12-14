package jp.kozu_osaka.android.kozuzen.net.callback;

public abstract class CallBack {

    protected static final int DEFAULT_MAXIMUM_RETRY = 3;

    /**
     * 現在のリトライ回数
     */
    protected int nowRetry;
}
