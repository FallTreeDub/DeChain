package jp.kozu_osaka.android.kozuzen.access.callback;

import org.jetbrains.annotations.NotNull;

import jp.kozu_osaka.android.kozuzen.access.DataBaseAccessor;
import jp.kozu_osaka.android.kozuzen.access.request.get.GetRequest;

public abstract class GetAccessCallBack<T> implements CallBack {

    private final GetRequest<T> getRequest;

    public GetAccessCallBack(GetRequest<T> getRequest) {
        this.getRequest = getRequest;
    }

    public abstract void onSuccess(@NotNull T responseResult);

    public abstract void onFailure();

    public abstract void onTimeOut();

    @Override
    public void retry() {
        DataBaseAccessor.sendGetRequest(this.getRequest, this);
    }
}
