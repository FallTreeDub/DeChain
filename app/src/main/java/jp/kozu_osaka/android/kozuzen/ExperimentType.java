package jp.kozu_osaka.android.kozuzen;

import org.jetbrains.annotations.Range;

/**
 * <p>
 *     DeChainでは、一日ごとに送信される通知の種類は{@link jp.kozu_osaka.android.kozuzen.ExperimentType}によって分類される。
 *     本登録アカウント作成時に、ユーザーごとに異なる{@code ExperimentType}がデータベース側から振り分けられる。
 * </p>
 * <p>
 *     振り分けられた{@code ExperimentType}は{@link jp.kozu_osaka.android.kozuzen.internal.InternalExperimentType}が管理する
 *     jsonファイルに保存され、通知送信時の分岐で使用される。
 * </p>
 *
 * @see jp.kozu_osaka.android.kozuzen.internal.InternalExperimentType
 */
public enum ExperimentType {

    /**
     * 通知送信なしの期間。
     */
    TYPE_NON_NOTIFICATION(0),

    /**
     * 肯定的、自分との比較
     */
    TYPE_POSITIVE_WITH_SELF(1),

    /**
     * 否定的、自分との比較
     */
    TYPE_NEGATIVE_WITH_SELF(2),

    /**
     * 肯定的、他人との比較
     */
    TYPE_POSITIVE_WITH_OTHER(3),

    /**
     * 否定的、他人との比較
     */
    TYPE_NEGATIVE_WITH_OTHER(4);

    private final int TYPE_ID;

    ExperimentType(int id) {
        this.TYPE_ID = id;
    }

    public int getID() {
        return this.TYPE_ID;
    }

    public static ExperimentType getFromID(int id) {
        for(ExperimentType t : values()) {
            if(t.getID() == id) return t;
        }
        return null;
    }
}
