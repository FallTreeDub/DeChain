package jp.kozu_osaka.android.kozuzen.access.callback;

public abstract class PostAccessCallBack {
    public abstract void onSuccess();

    public abstract void onFailure();

    public abstract void onTimeOut();
}
