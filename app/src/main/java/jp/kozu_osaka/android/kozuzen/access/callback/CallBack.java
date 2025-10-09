package jp.kozu_osaka.android.kozuzen.access.callback;

public interface CallBack {

    /**
     * 所定のリクエストを再度実行する。タイムアウト時などに使用する。
     */
    void retry();
}
