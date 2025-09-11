package jp.kozu_osaka.android.kozuzen.access.argument.post;

import java.util.List;
import java.util.Map;

import jp.kozu_osaka.android.kozuzen.access.argument.Arguments;

public final class PostArguments extends Arguments {

    private final Map<String, List<String>> KEY_VALUE_MAP;

    public PostArguments(Map<String, List<String>> keyValueMap) {
        this.KEY_VALUE_MAP = keyValueMap;
    }

    /**
     * Key-Value方式のMapへ変換する。
     * @return
     */
    public Map<String, List<String>> toMap() {
        return this.KEY_VALUE_MAP;
    }
}
