package jp.kozu_osaka.android.kozuzen.net.callback;

import android.content.Context;

import androidx.annotation.CallSuper;
import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import jp.kozu_osaka.android.kozuzen.CreateAccountActivity;
import jp.kozu_osaka.android.kozuzen.KozuZen;
import jp.kozu_osaka.android.kozuzen.R;
import jp.kozu_osaka.android.kozuzen.exception.PostAccessException;
import jp.kozu_osaka.android.kozuzen.net.DataBaseAccessor;
import jp.kozu_osaka.android.kozuzen.net.DataBaseGetResponse;
import jp.kozu_osaka.android.kozuzen.net.DataBasePostResponse;
import jp.kozu_osaka.android.kozuzen.net.request.post.PostRequest;
import jp.kozu_osaka.android.kozuzen.net.request.Request;

public abstract class PostAccessCallBack extends CallBack {

    private final PostRequest postRequest;

    public PostAccessCallBack(PostRequest postRequest) {
        this.postRequest = postRequest;
    }

    public abstract void onSuccess(@NotNull DataBasePostResponse response);

    public abstract void onFailure(@Nullable DataBasePostResponse response);

    public abstract void onTimeOut();

    @Override
    public void retry(int maximumRetry) {
        if(nowRetry <= maximumRetry) {
            DataBaseAccessor.sendPostRequest(this.postRequest, this);
            nowRetry++;
        }
    }
}
