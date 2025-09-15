package jp.kozu_osaka.android.kozuzen.access.callback;

import androidx.annotation.Nullable;

import jp.kozu_osaka.android.kozuzen.access.DataBasePostResponse;

public abstract class PostAccessCallBack {

    public abstract void onSuccess();

    /**
     *
     * @param response
     */
    public abstract void onFailure(@Nullable DataBasePostResponse response);

    public abstract void onTimeOut(DataBasePostResponse response);
}
