package jp.kozu_osaka.android.kozuzen.net.request.post;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.List;
import java.util.Map;

import jp.kozu_osaka.android.kozuzen.net.argument.post.PostArguments;
import jp.kozu_osaka.android.kozuzen.net.request.Request;
import jp.kozu_osaka.android.kozuzen.security.Secrets;

/**
 * データベースに対してPOSTリクエストを送るリクエスト。
 */
public class PostRequest extends Request {

    private static final String KEY_REQUEST_CODE = "operationID";
    private static final String KEY_APP_SIGNATURES = "signatures";
    private static final String KEY_ARGUMENTS = "arguments";

    protected final PostArguments arguments;

    protected PostRequest(RequestType type, PostArguments args) {
        super(type);
        this.arguments = args;
    }

    public String toJson() {
        JsonObject root = new JsonObject();
        root.addProperty(KEY_REQUEST_CODE, this.type.getRequestCode());

        JsonArray signatureArray = new JsonArray();
        String[] signatures = Secrets.getSignatureHexStringArray();
        if(signatures != null) {
            for(String s : signatures) {
                signatureArray.add(s);
            }
        }
        root.add(KEY_APP_SIGNATURES, signatureArray);

        //add arguments as a JsonElement to 'root'
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
}
