package jp.kozu_osaka.android.kozuzen.access.request;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.List;
import java.util.Map;

import jp.kozu_osaka.android.kozuzen.access.argument.Arguments;

/**
 * DeChainデータベースへの書き込みリクエストの実体。
 */
public class Request {

    private final RequestType type;
    private final Arguments arguments;
    private static final String KEY_REQUEST_CODE = "operationID";
    private static final String KEY_APP_SIGNATURE = "signature";
    private static final String KEY_ARGUMENTS = "arguments";

    protected Request(RequestType type, Arguments args) {
        this.type = type;
        this.arguments = args;
    }

    public RequestType getType() {
        return this.type;
    }

    public Arguments getArguments() {
        return this.arguments;
    }

    public String toJson() {
        JsonObject root = new JsonObject();
        root.addProperty(KEY_REQUEST_CODE, this.type.getRequestCode());

        //get app's signatures.
        //TODO: PackageInfo info = manager.getPackageInfo(KozuZen.getInstance().getPackageName(), PackageManager.GET_SIGNING_CERTIFICATES);
        //TODO: Signature[] signatures = info.signingInfo.getSigningCertificateHistory();
        //TODO: Arrays.stream(signatures).forEach(s -> hexStringSignatures.add(ZenHashEncrypter.hexHash(s.toCharsString())));
        root.addProperty(KEY_APP_SIGNATURE, "t"); // TODO: テスト用で[t]としているだけ。本番には上のコードに書き換え

        //add arguments as a JsonElement to 'root'.
        JsonObject argElements = new JsonObject();
        for(Map.Entry<String, List<String>> argEntry : arguments.toMap().entrySet()) {
            if(argEntry.getValue().size() == 1) {
                argElements.addProperty(argEntry.getKey(), argEntry.getValue().get(0));
                continue;
            }

            JsonArray argValues = new JsonArray();
            for(String argValue : argEntry.getValue()) {
                argValues.add(argValue);
            }
            argElements.add(argEntry.getKey(), argValues);
        }
        root.add(KEY_ARGUMENTS, argElements);
        return new Gson().toJson(root);
    }

    public enum RequestType {
        /**
         * 仮登録アカウントの作成のリクエスト。
         */
        REGISTER_TENTATIVE(0),

        /**
         * 本登録アカウントの作成リクエスト。
         */
        REGISTER(1),

        /**
         * アプリ使用時間の記録リクエスト。
         */
        REGISTER_USAGE_DATA(2),

        /**
         * 実験期間後の事後アンケートの回答記録リクエスト。
         */
        REGISTER_POST_SURVEY(5),

        /**
         *
         */
        SET_TENTATIVE_AUTHCODE_AUTHED(7),

        /**
         *
         */
        SET_TENTATIVE_AUTHCODE_RECREATE(8),

        /**
         *
         */
        SET_REQUEST_RESET_PASS(9),

        /**
         *
         */
        SET_RESET_PASS_AUTHCODE_AUTHED(10),

        /**
         *
         */
        SET_RESET_PASS_AUTHCODE_RECREATE(11);

        private final int REQUEST_CODE;
        RequestType(int requestCode) {
            this.REQUEST_CODE = requestCode;
        }

        public int getRequestCode() {
            return this.REQUEST_CODE;
        }
    }
}
