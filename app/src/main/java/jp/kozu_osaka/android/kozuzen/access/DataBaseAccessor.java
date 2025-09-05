package jp.kozu_osaka.android.kozuzen.access;

import androidx.annotation.NonNull;

import java.io.IOException;

import jp.kozu_osaka.android.kozuzen.access.request.post.PostRequest;
import jp.kozu_osaka.android.kozuzen.security.Secrets;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public final class DataBaseAccessor {

    private DataBaseAccessor() {}

    public static void sendPostRequest(PostRequest postRequest, AccessCallBack callBack) {
        OkHttpClient client = new OkHttpClient();
        MediaType mime = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(postRequest.toJson(), mime);
        okhttp3.Request request = new Request.Builder()
                .url(Secrets.ACCESS_QUERY_URL)
                .post(body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callBack.onFailure();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                DataBaseResponse res = DataBaseResponse.parse(response.body().string());

            }
        });
    }

    //public static T sendGetRequest(GetRequest<T> request) {}


}
