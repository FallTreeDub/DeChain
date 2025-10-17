package jp.kozu_osaka.android.kozuzen.access.callback;

import androidx.annotation.Nullable;

import jp.kozu_osaka.android.kozuzen.access.DataBaseAccessor;
import jp.kozu_osaka.android.kozuzen.access.DataBasePostResponse;
import jp.kozu_osaka.android.kozuzen.access.request.post.PostRequest;

public abstract class PostAccessCallBack extends CallBack {

    private final PostRequest postRequest;

    public PostAccessCallBack(PostRequest postRequest) {
        this.postRequest = postRequest;
    }

    public abstract void onSuccess();

    public abstract void onFailure(@Nullable DataBasePostResponse response);

    public abstract void onTimeOut(DataBasePostResponse response);

    @Override
    public void retry(int maximumRetry) {
        if(nowRetry <= maximumRetry) {
            DataBaseAccessor.sendPostRequest(postRequest, this);
            nowRetry++;
        }
    }
}
