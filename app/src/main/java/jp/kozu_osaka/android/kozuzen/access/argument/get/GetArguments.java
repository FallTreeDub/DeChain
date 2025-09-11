package jp.kozu_osaka.android.kozuzen.access.argument.get;

import java.util.List;
import java.util.Map;

import jp.kozu_osaka.android.kozuzen.access.argument.Arguments;

public class GetArguments extends Arguments {

    private final Map<String, String> KEY_VALUE_MAP;

    public GetArguments(Map<String, String> keyValueMap) {
        super();
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
