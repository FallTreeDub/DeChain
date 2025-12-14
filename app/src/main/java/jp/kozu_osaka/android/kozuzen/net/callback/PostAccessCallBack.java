package jp.kozu_osaka.android.kozuzen.net.callback;

import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import jp.kozu_osaka.android.kozuzen.net.DataBasePostResponse;
import jp.kozu_osaka.android.kozuzen.net.request.post.PostRequest;

public abstract class PostAccessCallBack extends CallBack {

    private final PostRequest postRequest;

    public PostAccessCallBack(PostRequest postRequest) {
        this.postRequest = postRequest;
    }

    public abstract void onSuccess(@NotNull DataBasePostResponse response);

    public abstract void onFailure(@Nullable DataBasePostResponse response);

    public abstract void onTimeOut();
}
