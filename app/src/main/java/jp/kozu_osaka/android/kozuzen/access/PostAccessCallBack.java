package jp.kozu_osaka.android.kozuzen.access;

public abstract class PostAccessCallBack extends AccessCallBack {
    public abstract void onSuccess();

    public abstract void onFailure();

    public abstract void onTimeOut();
}
