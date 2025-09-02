package jp.kozu_osaka.android.kozuzen.access;

import androidx.annotation.NonNull;

import java.io.IOException;

import jp.kozu_osaka.android.kozuzen.access.request.PostRequest;
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

    public static void sendPostRequest(PostRequest postReq) {
        OkHttpClient client = new OkHttpClient();
        MediaType mime = MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.create (mime, postReq.toJson());
        okhttp3.Request request = new Request.Builder()
                .url(Secrets.ACCESS_QUERY_URL)
                .post(requestBody)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                //
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                /**
                 * todo:
                 * Request内の抽象メソッド{@code onSuccess(), onFailed()}を起動
                 */
            }
        });
    }
}
