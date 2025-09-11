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

import jp.kozu_osaka.android.kozuzen.access.callback.GetAccessCallBack;
import jp.kozu_osaka.android.kozuzen.access.callback.PostAccessCallBack;
import jp.kozu_osaka.android.kozuzen.access.request.get.GetRequest;
import jp.kozu_osaka.android.kozuzen.access.request.post.PostRequest;
import jp.kozu_osaka.android.kozuzen.security.Secrets;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public final class DataBaseAccessor {

    private DataBaseAccessor() {}

    public static void sendPostRequest(PostRequest postRequest, PostAccessCallBack callBack) {
        OkHttpClient client = new OkHttpClient();
        MediaType mime = MediaType.parse("text/plain; charset=utf-8");
        RequestBody requestBody = RequestBody.create(postRequest.toJson(), mime);
        okhttp3.Request request = new Request.Builder()
                .url(Secrets.ACCESS_QUERY_URL)
                .post(requestBody)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callBack.onFailure();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                DataBasePostResponse strResponse = DataBasePostResponse.parse(response.body().string());
                switch(strResponse.getResponseCode()) {
                    case HttpURLConnection.HTTP_OK:
                        new Handler(Looper.getMainLooper()).post(callBack::onSuccess);
                        break;
                    case HttpURLConnection.HTTP_CLIENT_TIMEOUT:
                        new Handler(Looper.getMainLooper()).post(callBack::onTimeOut);
                        break;
                    default:
                        new Handler(Looper.getMainLooper()).post(callBack::onFailure);
                }
            }
        });
    }

    public static <T> void sendGetRequest(GetRequest<T> getRequest, GetAccessCallBack<T> callBack) {
        OkHttpClient client = new OkHttpClient();
        HttpUrl url = HttpUrl.parse(Secrets.ACCESS_QUERY_URL)
                .newBuilder()
                .addQueryParameter("requestID", String.valueOf(getRequest.getType().getRequestCode()))
                .build();
        okhttp3.Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callBack.onFailure();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                JsonObject jsonResponseRoot = JsonParser.parseString(response.body().string()).getAsJsonObject();
                switch(jsonResponseRoot.get("responseCode").getAsInt()) {
                    case HttpURLConnection.HTTP_OK:
                        new Handler(Looper.getMainLooper()).post(() -> {
                            callBack.onSuccess(getRequest.parseJsonResponse(jsonResponseRoot.get("result")));
                        });
                        break;
                    case HttpURLConnection.HTTP_CLIENT_TIMEOUT:
                        new Handler(Looper.getMainLooper()).post(callBack::onTimeOut);
                        break;
                    default:
                        new Handler(Looper.getMainLooper()).post(callBack::onFailure);
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
