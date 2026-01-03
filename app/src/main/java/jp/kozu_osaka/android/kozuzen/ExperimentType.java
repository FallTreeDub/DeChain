package jp.kozu_osaka.android.kozuzen;

/**
 * <p>
 *     DeChainでは、一日ごとに送信される通知の種類は{@link jp.kozu_osaka.android.kozuzen.ExperimentType}によって分類される。
 *     本登録アカウント作成時に、ユーザーごとに異なる{@code ExperimentType}がデータベース側から振り分けられる。
 * </p>
 */
public enum ExperimentType {

    /**
     * 通知送信なし。
     */
    TYPE_NON_NOTIFICATION(0, false, false),

    /**
     * 肯定的、自分との比較
     */
    TYPE_POSITIVE_WITH_SELF(1, true, true),

    /**
     * 否定的、自分との比較
     */
    TYPE_NEGATIVE_WITH_SELF(2, false, true),

    /**
     * 肯定的、他人との比較
     */
    TYPE_POSITIVE_WITH_OTHER(3, true, false),

    /**
     * 否定的、他人との比較
     */
    TYPE_NEGATIVE_WITH_OTHER(4, false, false);

    private final int TYPE_ID;
    private final boolean IS_POSITIVE;
    private final boolean IS_COMPARE_WITH_SELF;

    ExperimentType(int id, boolean isPositive, boolean isCompareWithSelf) {
        this.TYPE_ID = id;
        this.IS_POSITIVE = isPositive;
        this.IS_COMPARE_WITH_SELF = isCompareWithSelf;
    }

    public int getID() {
        return this.TYPE_ID;
    }

    public boolean isPositive() {
        return this.IS_POSITIVE;
    }

    public boolean isCompareWithSelf() {
        return this.IS_COMPARE_WITH_SELF;
    }

    public static ExperimentType getFromID(int id) {
        for(ExperimentType t : values()) {
            if(t.getID() == id) return t;
        }
        return null;
    }
}
