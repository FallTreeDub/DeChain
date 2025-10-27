package jp.kozu_osaka.android.kozuzen.access;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.concurrent.TimeUnit;

import jp.kozu_osaka.android.kozuzen.access.callback.GetAccessCallBack;
import jp.kozu_osaka.android.kozuzen.access.callback.PostAccessCallBack;
import jp.kozu_osaka.android.kozuzen.access.request.get.GetRequest;
import jp.kozu_osaka.android.kozuzen.access.request.post.PostRequest;
import jp.kozu_osaka.android.kozuzen.exception.GetAccessException;
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

    private static final String POST_REQUEST_JSON_KEY_SIGNATURE = "signature";
    private static final String POST_REQUEST_JSON_KEY_OPERATION_ID = "operationID";
    private static final String POST_REQUEST_JSON_KEY_ARGUMENTS = "arguments";
    private static final String GET_RESPONSE_JSON_KEY_RESULT = "result";
    private static final String GET_RESPONSE_JSON_KEY_RESPONSE_CODE = "responseCode";
    private static final String GET_RESPONSE_JSON_KEY_MESSAGE = "message";
    private static final String GET_REQUEST_PARAM_KEY_SIGNATURE = "signature";
    private static final String GET_REQUEST_PARAM_KEY_OPERATION_ID = "operationID";

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
        RequestBody requestBody = RequestBody.create(postRequest.toJson(), mime);
        okhttp3.Request request = new Request.Builder()
                .url(Secrets.ACCESS_QUERY_URL)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                new Handler(Looper.getMainLooper()).post(() -> callBack.onTimeOut(null));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if(response.body() == null) {
                    new Handler(Looper.getMainLooper()).post(() -> callBack.onFailure(null));
                    return;
                }
                DataBasePostResponse strResponse = DataBasePostResponse.parse(response.body().string());
                Logger.i(strResponse);
                switch(strResponse.getResponseCode()) {
                    case HttpURLConnection.HTTP_OK:
                        new Handler(Looper.getMainLooper()).post(() -> callBack.onSuccess(strResponse));
                        break;
                    case HttpURLConnection.HTTP_CLIENT_TIMEOUT:
                        new Handler(Looper.getMainLooper()).post(() -> callBack.onTimeOut(strResponse));
                        break;
                    default:
                        new Handler(Looper.getMainLooper()).post(() -> callBack.onFailure(strResponse));
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
        HttpUrl url = HttpUrl.parse(Secrets.ACCESS_QUERY_URL)
                .newBuilder()
                .addQueryParameter(GET_REQUEST_PARAM_KEY_OPERATION_ID, String.valueOf(getRequest.getType().getRequestCode()))
                .addQueryParameter(GET_REQUEST_PARAM_KEY_SIGNATURE, "t")
                .build();
        okhttp3.Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                new Handler(Looper.getMainLooper()).post(callBack::onTimeOut);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                JsonObject jsonResponseRoot = JsonParser.parseString(response.body().string()).getAsJsonObject();
                switch(jsonResponseRoot.get(GET_RESPONSE_JSON_KEY_RESPONSE_CODE).getAsInt()) {
                    case HttpURLConnection.HTTP_OK:
                        new Handler(Looper.getMainLooper()).post(() -> {
                            callBack.onSuccess(getRequest.parseJsonResponse(jsonResponseRoot.get(GET_RESPONSE_JSON_KEY_RESULT)));
                        });
                        break;
                    case HttpURLConnection.HTTP_CLIENT_TIMEOUT:
                        new Handler(Looper.getMainLooper()).post(callBack::onTimeOut);
                        break;
                    default:
                        new Handler(Looper.getMainLooper()).post(() -> {
                            callBack.onFailure(
                                    jsonResponseRoot.get(GET_RESPONSE_JSON_KEY_RESPONSE_CODE).getAsInt(),
                                    jsonResponseRoot.get(GET_RESPONSE_JSON_KEY_MESSAGE).getAsString()
                            );
                        });
                }
            }
        });
    }

    public static void showLoadFragment(FragmentActivity activity, @IdRes int fragmentFrameId) {
        FragmentManager manager = activity.getSupportFragmentManager();
        if(manager.findFragmentByTag(LoadingFragment.LOADING_FRAGMENT_TAG) == null) {
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.add(fragmentFrameId, new LoadingFragment(), LoadingFragment.LOADING_FRAGMENT_TAG).commit();
        }
    }

    public static void removeLoadFragment(FragmentActivity activity) {
        FragmentManager manager = activity.getSupportFragmentManager();
        Fragment loadingFragment = manager.findFragmentByTag(LoadingFragment.LOADING_FRAGMENT_TAG);
        if(loadingFragment != null) {
            manager.beginTransaction().remove(loadingFragment).commit();
        }
    }
}
