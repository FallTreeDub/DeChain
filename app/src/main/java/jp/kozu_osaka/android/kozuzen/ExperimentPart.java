package jp.kozu_osaka.android.kozuzen;

/**
 * 1週間ごとに分けられる実験期間のパート。
 */
public enum ExperimentPart {

    /**
     * 通知送信なしの期間。
     */
    PART_1(),

    /**
     * 肯定的、自分との比較
     */
    PART_2(),

    /**
     * 否定的、自分との比較
     */
    PART_3(),

    /**
     * 肯定的、他人との比較
     */
    PART_4(),

    /**
     * 否定的、他人との比較
     */
    PART_5();

    /**
     * 期間の初めの日
     */
    private final int START_DAY;

    /**
     * 期間の終了日。
     */
    private final int END_DAY;

    //todo: start_dayとend_dayの設定
}
