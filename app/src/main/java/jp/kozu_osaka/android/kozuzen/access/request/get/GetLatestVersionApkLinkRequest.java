package jp.kozu_osaka.android.kozuzen.access.request.get;

import com.google.gson.JsonElement;

import jp.kozu_osaka.android.kozuzen.access.argument.get.GetArguments;
import jp.kozu_osaka.android.kozuzen.access.argument.get.GetLatestVersionApkLinkArguments;

public final class GetLatestVersionApkLinkRequest extends GetRequest<String> {

    public GetLatestVersionApkLinkRequest(GetLatestVersionApkLinkArguments args) {
        super(RequestType.GET_LATEST_VERSION_APK_LINK, args);
    }

    @Override
    public String parseJsonResponse(JsonElement jsonElement) {
        return jsonElement.getAsString();
    }
}
