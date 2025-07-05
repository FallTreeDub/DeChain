package jp.kozu_osaka.android.kozuzen.access.argument;

import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

import jp.kozu_osaka.android.kozuzen.Constants;

public class Arguments {
    private final Map<String, String> KEY_VALUE_MAP;

    /**
     * {@code Arguments}のコンストラクタ。
     * @param keyValueMap
     */
    protected Arguments(Map<String, String> keyValueMap) {
        this.KEY_VALUE_MAP = keyValueMap;
    }

    /**
     * サブクラス内では、特定キーの値を外部クラスを参照させるときに使用されるべきである。
     * @param key
     * @return
     */
    protected String getValueOf(String key) {
        return this.KEY_VALUE_MAP.get(key);
    }

    /**
     * Key-Value方式のMapへ変換する。
     * @return
     */
    public Map<String, String> toMap() {
        return this.KEY_VALUE_MAP;
    }

    /**
     * {@code Arguments}のインスタンスに格納されたすべての引数のキーと値のペアを、
     * SpreadSheetのアクセスキューに適合する文字列に変換する。
     * 格納された引数がない場合は空文字列となる。
     * @return {@code String}に変換された引数のキーと値のペア。
     */
    @Override
    @NotNull
    public String toString() {
        StringBuilder builder = new StringBuilder();
        int i = 0;
        for(Map.Entry<String, String> entry : this.KEY_VALUE_MAP.entrySet()) {
            builder.append(entry.getKey()).append("=").append("\"").append(entry.getValue()).append("\"");
            if(i < this.KEY_VALUE_MAP.size() - 1) {
                builder.append(",");
            }
            i++;
        }
        return builder.toString().replaceAll("\n", "");
    }
}
