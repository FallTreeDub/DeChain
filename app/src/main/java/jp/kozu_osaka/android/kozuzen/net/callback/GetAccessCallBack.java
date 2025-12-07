package jp.kozu_osaka.android.kozuzen.net.callback;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import jp.kozu_osaka.android.kozuzen.net.DataBaseAccessor;
import jp.kozu_osaka.android.kozuzen.net.DataBaseGetResponse;
import jp.kozu_osaka.android.kozuzen.net.request.get.GetRequest;

public abstract class GetAccessCallBack<T> extends CallBack {

    protected final GetRequest<T> getRequest;

    public GetAccessCallBack(GetRequest<T> getRequest) {
        this.getRequest = getRequest;
    }

    public abstract void onSuccess(@NotNull DataBaseGetResponse response);

    public abstract void onFailure(@Nullable DataBaseGetResponse response);

    public abstract void onTimeOut();

    @Override
    public void retry(int maximumRetry) {
        if(nowRetry <= maximumRetry) {
            DataBaseAccessor.sendGetRequest(this.getRequest, this);
            nowRetry++;
        }
    }
}
