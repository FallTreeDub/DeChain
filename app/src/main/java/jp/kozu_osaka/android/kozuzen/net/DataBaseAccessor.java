package jp.kozu_osaka.android.kozuzen.net;

import android.app.Activity;
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

/**
 * {@link jp.kozu_osaka.android.kozuzen.net.request.Request}、{@link jp.kozu_osaka.android.kozuzen.net.callback.CallBack}などを用いて、実際に
 * DeChainDataBaseQueryとHTTPアクセスを行うことでデータベースにアクセスする。HTTP通信には{@link okhttp3.OkHttp}を用いる。
 */
public final class DataBaseAccessor {

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .callTimeout(60, TimeUnit.SECONDS)
            .build();

    private DataBaseAccessor() {}

    /**
     * データベースに対してPOSTリクエストを送信する。
     * 送信は非同期処理で実行され、{@code callBack}にてコールバックを定義する。
     * コールバック内の処理はUIスレッド上で実行される。
     * @param postRequest リクエストの内容。
     * @param callBack データベースからの応答が返ってきたときに実行されるコールバック。
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
     * 送信は非同期処理で実行され、{@code callBack}にてコールバックを定義する。
     * コールバック内の処理はUIスレッド上で実行される。
     * @param getRequest リクエストの内容。
     * @param callBack データベースからの応答が返ってきたときに実行されるコールバック。
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

    /**
     * データベースに対してPOSTリクエストを送信する。
     * 送信は同期処理で実行されるため、使用の際はUIスレッド上で動かしてはいけない。
     * @param postRequest リクエストの内容。
     * @return データベースからの応答。
     * @throws IOException 通信のキャンセル、タイムアウト、通信エラーのとき。
     */
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
     * データベースに対してGETリクエストを送信する。
     * 送信は同期処理で実行されるため、使用の際はUIスレッド上で動かしてはいけない。
     * @param getRequest リクエストの内容。
     * @return データベースからの応答。
     * @throws IOException 通信のキャンセル、タイムアウト、通信エラーのとき。
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

    /**
     * {@link GetRequest}からokhttp用の{@link Request}を作成する。
     * @param dechainRequest {@link GetRequest}
     */
    private static <T> Request buildGetRequest(GetRequest<T> dechainRequest) {
        HttpUrl url = HttpUrl.parse(Secrets.ACCESS_QUERY_URL + dechainRequest.toURLParam())
                .newBuilder()
                .build();
        return new Request.Builder()
                .url(url)
                .get()
                .build();
    }

    /**
     * {@link PostRequest}からokhttp用の{@link Request}を作成する。
     * @param dechainRequest {@link PostRequest}
     */
    private static Request buildPostRequest(PostRequest dechainRequest) {
        MediaType mime = MediaType.parse("text/plain; charset=utf-8");
        RequestBody requestBody = RequestBody.create(dechainRequest.toJson(), mime);
        return new Request.Builder()
                .url(Secrets.ACCESS_QUERY_URL)
                .post(requestBody)
                .build();
    }

    /**
     * ロード画面を表示する。
     * @param activity ロード画面を表示させたい{@link Activity}。
     * @param fragmentFrameId {@code activity}の示すアクティビティ上にある、{@link Fragment}格納用の{@link FrameLayout}。
     */
    public static void showLoadFragment(FragmentActivity activity, @IdRes int fragmentFrameId) {
        activity.findViewById(fragmentFrameId).setVisibility(View.VISIBLE);
        FragmentManager manager = activity.getSupportFragmentManager();
        if(manager.findFragmentByTag(LoadingFragment.LOADING_FRAGMENT_TAG) == null) {
            FragmentTransaction transaction = manager.beginTransaction();
            LoadingFragment f = new LoadingFragment();
                    transaction.replace(fragmentFrameId, f, LoadingFragment.LOADING_FRAGMENT_TAG).commitNow();
        }
    }

    /**
     * ロード画面を削除する。ロード画面を表示させていない場合は無視される。
     * @param activity ロード画面を表示させている{@link Activity}。
     */
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
