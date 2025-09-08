package jp.kozu_osaka.android.kozuzen.access;

import org.jetbrains.annotations.NotNull;

public abstract class GetAccessCallBack<T> extends AccessCallBack {

    public abstract void onSuccess(@NotNull T responseResult);

    public abstract void onFailure();

    public abstract void onTimeOut();
}
