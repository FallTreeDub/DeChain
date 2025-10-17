package jp.kozu_osaka.android.kozuzen.access.callback;

import org.jetbrains.annotations.NotNull;

import jp.kozu_osaka.android.kozuzen.access.DataBaseAccessor;
import jp.kozu_osaka.android.kozuzen.access.request.get.GetRequest;

public abstract class GetAccessCallBack<T> extends CallBack {

    private final GetRequest<T> getRequest;

    public GetAccessCallBack(GetRequest<T> getRequest) {
        this.getRequest = getRequest;
    }

    public abstract void onSuccess(@NotNull T responseResult);

    public abstract void onFailure();

    public abstract void onTimeOut();

    @Override
    public void retry(int maximumRetry) {
        if(nowRetry <= maximumRetry) {
            DataBaseAccessor.sendGetRequest(this.getRequest, this);
            nowRetry++;
        }
    }
}
