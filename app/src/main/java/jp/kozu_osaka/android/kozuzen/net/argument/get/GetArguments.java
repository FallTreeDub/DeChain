package jp.kozu_osaka.android.kozuzen.net.argument.get;

import java.util.Map;

import jp.kozu_osaka.android.kozuzen.net.argument.Arguments;

public class GetArguments extends Arguments {

    private final Map<String, String> KEY_VALUE_MAP;

    public GetArguments(Map<String, String> keyValueMap) {
        this.KEY_VALUE_MAP = keyValueMap;
    }

    /**
     * Key-Value方式のMapへ変換する。
     * @return
     */
    public Map<String, String> toMap() {
        return this.KEY_VALUE_MAP;
    }
}
