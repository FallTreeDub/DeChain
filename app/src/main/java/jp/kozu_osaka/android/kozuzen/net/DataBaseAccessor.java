package jp.kozu_osaka.android.kozuzen.net;

import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.concurrent.TimeUnit;

import jp.kozu_osaka.android.kozuzen.net.callback.GetAccessCallBack;
import jp.kozu_osaka.android.kozuzen.net.callback.PostAccessCallBack;
import jp.kozu_osaka.android.kozuzen.net.request.get.GetRequest;
import jp.kozu_osaka.android.kozuzen.net.request.post.PostRequest;
import jp.kozu_osaka.android.kozuzen.security.Secrets;
import jp.kozu_osaka.android.kozuzen.util.Logger;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public final class DataBaseAccessor {
    private static final OkHttpClient client = new OkHttpClient.Builder()
            .callTimeout(60, TimeUnit.SECONDS)
            .build();

    private DataBaseAccessor() {}

    /**
     * データベースに対してPOSTリクエストを送信する。
     * 送信はこのメソッド内で非同期処理で実行され、{@code callBack}にてコールバックを定義する。
     *
     * コールバック内の処理はUIメソッド上で実行される。
     * @param postRequest
     * @param callBack
     */
    public static void sendPostRequest(PostRequest postRequest, PostAccessCallBack callBack) {
        client.newCall(buildPostRequest(postRequest)).enqueue(new Callback() {

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                new Handler(Looper.getMainLooper()).post(callBack::onTimeOut);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if(response.body() == null) {
                    new Handler(Looper.getMainLooper()).post(() -> callBack.onFailure(null));
                    return;
                }
                DataBasePostResponse strResponse = DataBasePostResponse.parse(response.body().string());
                switch(strResponse.getResponseCode()) {
                    case jp.kozu_osaka.android.kozuzen.net.request.Request.RESPONSE_CODE_NO_ERROR_WITH_MESSAGE:
                    case jp.kozu_osaka.android.kozuzen.net.request.Request.RESPONSE_CODE_NO_ERROR:
                        new Handler(Looper.getMainLooper()).post(() -> callBack.onSuccess(strResponse));
                        break;
                    default:
                        if(response.code() == HttpURLConnection.HTTP_CLIENT_TIMEOUT) {
                            new Handler(Looper.getMainLooper()).post(callBack::onTimeOut);
                        } else {
                            new Handler(Looper.getMainLooper()).post(() -> callBack.onFailure(strResponse));
                        }
                }
            }
        });
    }

    /**
     * データベースに対してGETリクエストを送信する。
     * 送信はこのメソッド内で非同期処理で実行され、{@code callBack}にてコールバックを定義する。
     *
     * コールバック内の処理はUIメソッド上で実行される。
     * @param getRequest
     * @param callBack
     * @param <T>
     */
    public static <T> void sendGetRequest(GetRequest<T> getRequest, GetAccessCallBack<T> callBack) {
        client.newCall(buildGetRequest(getRequest)).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                new Handler(Looper.getMainLooper()).post(callBack::onTimeOut);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if(response.body() == null) {
                    Logger.i(response.code());
                    new Handler(Looper.getMainLooper()).post(() -> callBack.onFailure(null));
                    return;
                }
                DataBaseGetResponse dbResponse = DataBaseGetResponse.parse(response.body().string());
                Logger.i(dbResponse.getResponseCode());
                switch(dbResponse.getResponseCode()) {
                    case jp.kozu_osaka.android.kozuzen.net.request.Request.RESPONSE_CODE_NO_ERROR_WITH_MESSAGE:
                    case jp.kozu_osaka.android.kozuzen.net.request.Request.RESPONSE_CODE_NO_ERROR:
                        new Handler(Looper.getMainLooper()).post(() -> {
                            callBack.onSuccess(dbResponse);
                        });
                        break;
                    default:
                        if(response.code() == HttpURLConnection.HTTP_CLIENT_TIMEOUT) {
                            new Handler(Looper.getMainLooper()).post(callBack::onTimeOut);
                        } else {
                            Logger.i(dbResponse.getResponseCode() + ", dechain");
                            new Handler(Looper.getMainLooper()).post(() -> {
                                callBack.onFailure(dbResponse);
                            });
                        }
                }
            }
        });
    }

    @Nullable
    public static DataBasePostResponse sendPostRequestSynchronous(PostRequest postRequest) throws IOException {
        try(Response response = client.newCall(buildPostRequest(postRequest)).execute()) {
            if(response.body() == null) {
                return null;
            }
            return DataBasePostResponse.parse(response.body().string());
        }
    }

    /**
     * 同期的にGETリクエストを送信する。UIメソッド上で呼び出してはいけない。
     * @throws IOException 接続に問題があった場合。
     * @return データベースからのレスポンス。
     */
    @Nullable
    public static <T> DataBaseGetResponse sendGetRequestSynchronous(GetRequest<T> getRequest) throws IOException {
        try(Response response = client.newCall(buildGetRequest(getRequest)).execute()) {
            if(response.body() == null) {
                return null;
            }
            return DataBaseGetResponse.parse(response.body().string());
        }
    }

    private static <T> Request buildGetRequest(GetRequest<T> dechainRequest) {
        HttpUrl url = HttpUrl.parse(Secrets.ACCESS_QUERY_URL + dechainRequest.toURLParam())
                .newBuilder()
                .build();
        return new Request.Builder()
                .url(url)
                .get()
                .build();
    }

    private static Request buildPostRequest(PostRequest dechainRequest) {
        MediaType mime = MediaType.parse("text/plain; charset=utf-8");
        RequestBody requestBody = RequestBody.create(dechainRequest.toJson(), mime);
        return new Request.Builder()
                .url(Secrets.ACCESS_QUERY_URL)
                .post(requestBody)
                .build();
    }


    public static void showLoadFragment(FragmentActivity activity, @IdRes int fragmentFrameId) {
        activity.findViewById(fragmentFrameId).setVisibility(View.VISIBLE);
        FragmentManager manager = activity.getSupportFragmentManager();
        if(manager.findFragmentByTag(LoadingFragment.LOADING_FRAGMENT_TAG) == null) {
            FragmentTransaction transaction = manager.beginTransaction();
            LoadingFragment f = new LoadingFragment();
                    transaction.replace(fragmentFrameId, f, LoadingFragment.LOADING_FRAGMENT_TAG).commitNow();
        }
    }

    public static void removeLoadFragment(FragmentActivity activity) {
        FragmentManager manager = activity.getSupportFragmentManager();
        Fragment loadingFragment = manager.findFragmentByTag(LoadingFragment.LOADING_FRAGMENT_TAG);
        if(loadingFragment != null) {
            if(loadingFragment.getView() != null) {
                ((FrameLayout)loadingFragment.getView().getParent()).setVisibility(View.GONE);
            }
            manager.beginTransaction().remove(loadingFragment).commit();
        }

    }
}
