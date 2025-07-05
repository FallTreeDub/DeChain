package jp.kozu_osaka.android.kozuzen.access;

import android.util.Log;

import androidx.annotation.Nullable;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.gson.Gson;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import jp.kozu_osaka.android.kozuzen.Constants;
import jp.kozu_osaka.android.kozuzen.KozuZen;
import jp.kozu_osaka.android.kozuzen.R;
import jp.kozu_osaka.android.kozuzen.access.request.Request;
import jp.kozu_osaka.android.kozuzen.annotation.InterruptibleMethod;
import jp.kozu_osaka.android.kozuzen.annotation.RunOnSubMethod;
import jp.kozu_osaka.android.kozuzen.security.HashedString;
import jp.kozu_osaka.android.kozuzen.security.Secrets;
import jp.kozu_osaka.android.kozuzen.security.SixNumberCode;

/**
 * データベースSpreadSheetへアクセスするクラス。
 */
public final class DeChainSpreadSheet {

    private static Sheets.Spreadsheets DB_SPREAD = null;

    /**
     * データベースへの書き込み用リクエストをDB内のリクエストキューに対して送信する。
     * @annotation {@link InterruptibleMethod}
     */
    @InterruptibleMethod
    public static void sendRequestToQueue(Request request) throws ExecutionException, IOException {
        ValueRange range = new ValueRange().setValues(Collections.singletonList(Arrays.asList(
                request.getType().getRequestCode(), request.getArguments().toString()
        )));
        HttpResponse response = null;
        HttpRequest httpRequest = DB_SPREAD.values().append(
                Secrets.SPREADSHEET_ID,
                Secrets.SPREADSHEET_QUEUE_SHEET_NAME + "!A3",
                range
        ).setValueInputOption("RAW").setInsertDataOption("INSERT_ROWS").buildHttpRequest();

        Future<HttpResponse> future = httpRequest.executeAsync();
        try {
            Log.i(Constants.Debug.LOGNAME_INFO, "appending");
            response = future.get();
        } catch(InterruptedException interruptEx) {
            Log.i(Constants.Debug.LOGNAME_INFO, "Sending request was interrupted.");
        } finally {
            if(response != null) response.disconnect();
        }
        Log.i(Constants.Debug.LOGNAME_INFO, "send request done");
    }

    /**
     * 仮登録アカウントシートに{@code mail}をメールアドレスとするアカウントがあるかを判定する。
     * 非同期処理上での実行が必要。
     * @param mail メールアドレス。
     * @return アカウントの有無。
     * @annotation {@link InterruptibleMethod}
     * @throws ExecutionException SpreadSheetへのアクセス時に発生した例外。
     * @throws IOException SpreadSheetへのアクセス時に発生した例外。
     */
    @InterruptibleMethod
    public static boolean existsTentativeAccount(String mail, HashedString pass) throws ExecutionException, IOException {
        HttpResponse response = null;
        ValueRange range;
        HttpRequest request = DB_SPREAD.values().get(
                Secrets.SPREADSHEET_ID,
                Secrets.SPREADSHEET_TENTATIVE_SHEET_NAME + "!A3:C").buildHttpRequest();
        Future<HttpResponse> future = request.executeAsync();
        try {
            response = future.get();
            range = response.parseAs(ValueRange.class);
        } catch(InterruptedException e) {
            return false;
        } finally {
            if(response != null) response.disconnect();
        }

        boolean exists = false;
        if(range.getValues() != null) {
            for(List<Object> line : range.getValues()) {
                if(Thread.currentThread().isInterrupted()) {
                    break;
                }
                if(line.get(0).equals(mail)) {
                    if(line.get(2).equals(pass.toString())) {
                        exists = true;
                    }
                    break;
                }
            }
        }
        return exists;
    }

    /**
     *
     * @param mail
     * @param pass
     * @return
     * @throws IOException
     * @annotation {@link InterruptibleMethod}
     */
    @InterruptibleMethod
    public static boolean existsRegisteredAccount(String mail, HashedString pass) throws ExecutionException, IOException {
        HttpResponse response = null;
        ValueRange range;
        HttpRequest request = DB_SPREAD.values().get(
                Secrets.SPREADSHEET_ID,
                Secrets.SPREADSHEET_REGISTERED_SHEET_NAME + "!A3:B").buildHttpRequest();
        Future<HttpResponse> future = request.executeAsync();
        try {
            response = future.get();
            range = response.parseAs(ValueRange.class);
        } catch(InterruptedException interruptEx) {
            return false;
        } finally {
            if(response != null) response.disconnect();
        }

        boolean exists = false;
        if(range.getValues() != null) {
            for(List<Object> line : range.getValues()) {
                if(Thread.currentThread().isInterrupted()) {
                    break;
                }
                if(line.get(0).equals(mail)) {
                    if(line.get(1).equals(pass.toString())) {
                        exists = true;
                    }
                    break;
                }
            }
        }
        return exists;
    }

    /**
     * パスワードの検証なしに、メールアドレスが本登録のアカウント群に含まれるかを検査する。
     * @param mail 存在を検査される対象のメールアドレス。
     * @return メールアドレスが本登録のアカウント群に含まれるか。
     * @throws IOException SpreadSheetからの情報取得に問題が発生した場合。
     * @annotation {@link InterruptibleMethod}
     */
    @InterruptibleMethod
    public static boolean existsRegisteredMail(String mail) throws ExecutionException, IOException {
        ValueRange range;
        HttpResponse response = null;
        HttpRequest request = DB_SPREAD.values().get(
                Secrets.SPREADSHEET_ID,
                Secrets.SPREADSHEET_REGISTERED_SHEET_NAME + "!A3:A").buildHttpRequest();
        Future<HttpResponse> future = request.executeAsync();
        try {
            response = future.get();
            range = response.parseAs(ValueRange.class);
        } catch(InterruptedException interruptEx) {
            return false;
        } finally {
            if(response != null) response.disconnect();
        }

        boolean exists = false;
        if(range.getValues() != null) {
            for(List<Object> line : range.getValues()) {
                if(Thread.currentThread().isInterrupted()) {
                    break;
                }
                if(line.get(0).equals(mail)) {
                    exists = true;
                    break;
                }
            }
        }
        return exists;
    }

    /**
     * DeChainアプリ内でユーザーが入力した6桁コード({@code appUserEnteredCode})が、
     * {@code mail}に対応したSpreadSheet上の仮登録アカウントのものと一致するかを判定する。
     * @param mail SpreadSheet上の仮登録アカウントのメールアドレス。
     * @param appUserEnteredCode DeChainアプリ内でユーザーが入力した6桁コード。
     * @return 一致するかどうか。
     * @annotation {@link InterruptibleMethod}
     */
    @InterruptibleMethod
    public static boolean isCorrectAuthCode(String mail, SixNumberCode appUserEnteredCode) throws ExecutionException, IOException {
        ValueRange range;
        HttpResponse response = null;
        HttpRequest request = DB_SPREAD.values().get(
                Secrets.SPREADSHEET_ID,
                appUserEnteredCode.getType().getTargetSheetName() + "!A3:" + appUserEnteredCode.getType().getTargetColumn()).buildHttpRequest();

        Future<HttpResponse> future = request.executeAsync();
        try {
            response = future.get();
            range = response.parseAs(ValueRange.class);
        } catch(InterruptedException interruptEx) {
            return false;
        } finally {
            if(response != null) response.disconnect();
        }

        boolean correct = false;
        for(List<Object> line : range.getValues()) {
            if(Thread.currentThread().isInterrupted()) {
                break;
            }
            if(line.get(0).equals(mail)) {
                if(line.get(line.size() - 1).equals(appUserEnteredCode.getCode())) {
                    correct = true;
                }
                break;
            }
        }
        return correct;
    }

    /**
     * DeChainSpreadSheetクラス内でスプレッドシートへのアクセスができるよう準備をする。
     * @param serviceAccount SpreadSheetへアクセスするためのGoogleサービスアカウント。
     * @throws ExecutionException スプレッドシートの取得に問題が発生した場合。
     */
    public static void init(ServiceAccount serviceAccount) throws ExecutionException {
        ExecutorService service = Executors.newFixedThreadPool(1);

        Future<Sheets.Spreadsheets> future = service.submit(new Callable<Sheets.Spreadsheets>() {
            @Override
            public Sheets.Spreadsheets call() throws Exception {
                InputStream jsonStream = new ByteArrayInputStream(new Gson().toJson(serviceAccount).getBytes());
                GoogleCredentials credentials = ServiceAccountCredentials.fromStream(jsonStream);
                HttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
                JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
                HttpRequestInitializer initializer = new HttpCredentialsAdapter(credentials);

                return new Sheets.Builder(transport, jsonFactory, initializer)
                        .setApplicationName(KozuZen.getInstance().getString(R.string.app_name))
                        .build()
                        .spreadsheets();
            }
        });

        try {
            DB_SPREAD = future.get();
            Log.i(Constants.Debug.LOGNAME_INFO, String.valueOf(DB_SPREAD == null));
        } catch (InterruptedException e) {
            Log.i(Constants.Debug.LOGNAME_INFO, "spreadsheet getting service was interrupted.");
        }
    }
}
