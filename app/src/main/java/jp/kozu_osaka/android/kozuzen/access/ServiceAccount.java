package jp.kozu_osaka.android.kozuzen.access;

import android.content.pm.PackageManager;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.api.client.util.Lists;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import jp.kozu_osaka.android.kozuzen.Constants;
import jp.kozu_osaka.android.kozuzen.KozuZen;
import jp.kozu_osaka.android.kozuzen.security.Secrets;
import jp.kozu_osaka.android.kozuzen.security.exception.IllegalSignatureException;

import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * <p>
 *     SpreadSheet API v4を通じてSpreadsheetにアクセスするために必要な、サービスアカウントの実体。
 * </p>
 * <p>
 *     Google Apps Scriptによってスクリプトされた、DeChainのアプリ署名を持ったアプリからのアクセスのみを受け付けるウェブサイトから
 *     サービスアカウントのjsonファイルを受け取り、{@link Gson}によるオブジェクト化を用いてjavaオブジェクト化を実現している。
 * </p>
 *
 * @see ServiceAccount#get()
 */
public final class ServiceAccount {

    /**
     * 1度アクセス後、再度サイトへのアクセスが必要にならないために内部にインスタンスを保有する。
     */
    private static ServiceAccount instance = null;

    private String type;
    private String project_id;
    private String private_key_id;
    private String private_key;
    private String client_email;
    private String client_id;
    private String auth_uri;
    private String token_uri;
    private String auth_provider_x509_cert_url;
    private String client_x509_cert_url;
    private String universe_domain;

    private ServiceAccount() {}

    /**
     * <p>
     *     Google Apps Script(以下、GAS)でスクリプトされたサイトからGoogleのサービスアカウントを取得する。
     * </p>
     * <p>
     *     サイトは、DeChain開発によるAndroidアプリ署名がパラメータとして渡されることでサービスアカウントのjsonを返す。
     *     正当な署名でない場合、または署名が存在しない場合は{@link IllegalSignatureException}がスローされる。
     * </p>
     * <p>
     *     このメソッドはHTML接続を行うため、ExecutorServiceで非同期処理として実行される。
     *     そのため、途中でinterruptされると{@code null}が戻り値となる。
     * </p>
     * @return 取得されたGoogleサービスアカウント。
     * @throws IllegalSignatureException アプリ内の署名がDeChain開発者によって署名されていない場合。
     * @throws IOException 接続の問題でGASサイトへのアクセスができない場合。
     * @throws PackageManager.NameNotFoundException Android上に正式にアプリとしてDeChainがインストールされておらず、署名が取得できない場合。
     */
    @Nullable
    public static ServiceAccount get() throws IllegalSignatureException, IOException, PackageManager.NameNotFoundException, ExecutionException {

        if(instance != null) return instance;

        PackageManager manager = KozuZen.getInstance().getPackageManager();
        List<String> hexStringSignatures = Lists.newArrayList();
        try {
            //アプリに付属している署名を取得
            //todo: PackageInfo info = manager.getPackageInfo(KozuZen.getInstance().getPackageName(), PackageManager.GET_SIGNING_CERTIFICATES);
            //todo: Signature[] signatures = info.signingInfo.getSigningCertificateHistory();
            //todo: Arrays.stream(signatures).forEach(s -> hexStringSignatures.add(ZenHashEncrypter.hexHash(s.toCharsString())));
            hexStringSignatures.add("t"); // TODO: テスト用で[t]としているだけ．本番には上のコードに書き換え
        } catch(NullPointerException e) { //アプリの署名が存在しない場合
            throw new IllegalSignatureException("This app is not signed by DeChain team.");
        }

        ExecutorService service = Executors.newFixedThreadPool(1);
        Future<ServiceAccount> future = service.submit(new Callable<ServiceAccount>() {
            @Override
            public ServiceAccount call() throws Exception {
                OkHttpClient client = new OkHttpClient();
                HttpUrl.Builder builder = HttpUrl.parse(Secrets.SERVICE_ACCOUNT_JSON_PROVIDER_URL).newBuilder();
                hexStringSignatures.forEach(s -> builder.addQueryParameter("appSignature", s));
                Request getRequest = new Request.Builder().url(builder.build()).build();
                Call getCall = client.newCall(getRequest);
                try(Response getResponse = getCall.execute()) { //GETリクエスト送信、responseとして結果を受け取る
                    String serviceAccountJson = getResponse.body().string();//message部分を取得
                    if(serviceAccountJson.isEmpty()) throw new IllegalSignatureException("This app is not signed by DeChain team."); //GASからの返答のstringがempty -> 署名が正当ではない
                    ServiceAccount serviceAccount = new Gson().fromJson(serviceAccountJson, ServiceAccount.class);
                    instance = serviceAccount;
                    return serviceAccount;
                } catch(NullPointerException e) { //GASからの返答がnull -> 署名が正当ではない
                    throw new IllegalSignatureException("This app is not signed by DeChain team.");
                }
            }
        });

        try {
            return future.get();
        } catch(InterruptedException e) {
            Log.i(Constants.Debug.LOGNAME_INFO, "Service account service was interrupted.");
        }
        return null;
    }
}
