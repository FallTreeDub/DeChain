package jp.kozu_osaka.android.kozuzen.net;

import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
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

    private static final String GET_RESPONSE_JSON_KEY_RESULT = "result";
    private static final String GET_RESPONSE_JSON_KEY_RESPONSE_CODE = "responseCode";
    private static final String GET_RESPONSE_JSON_KEY_MESSAGE = "message";

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
        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(20, TimeUnit.SECONDS)
                .build();
        MediaType mime = MediaType.parse("text/plain; charset=utf-8");
        Logger.i(postRequest.toJson());
        RequestBody requestBody = RequestBody.create(postRequest.toJson(), mime);
        okhttp3.Request request = new Request.Builder()
                .url(Secrets.ACCESS_QUERY_URL)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {

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
        OkHttpClient client = new OkHttpClient();

        HttpUrl url = HttpUrl.parse(Secrets.ACCESS_QUERY_URL + getRequest.toURLParam())
                .newBuilder()
                .build();
        okhttp3.Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        Logger.i(url.url().toString());

        client.newCall(request).enqueue(new Callback() {
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
                DataBaseGetResponse dbResponse = DataBaseGetResponse.parse(response.body().string());
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
                            new Handler(Looper.getMainLooper()).post(() -> {
                                callBack.onFailure(dbResponse);
                            });
                        }
                }
            }
        });
    }

    public static void showLoadFragment(FragmentActivity activity, @IdRes int fragmentFrameId) {
        activity.findViewById(fragmentFrameId).setVisibility(View.VISIBLE);
        FragmentManager manager = activity.getSupportFragmentManager();
        if(manager.findFragmentByTag(LoadingFragment.LOADING_FRAGMENT_TAG) == null) {
            Logger.i("tag is null");
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
            manager.beginTransaction().remove(loadingFragment).commitNow();
        }

    }
}
