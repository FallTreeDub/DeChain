package jp.kozu_osaka.android.kozuzen.security;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import jp.kozu_osaka.android.kozuzen.Constants;

/**
 * <p>アカウント作成時の質問の時に何期生であるかを質問する際、入力された値が高津高校現役であるかを判定する。</p>
 * <p>計算方法は{@link TermChecker#checkTerm(String termStr)}のドキュメントを参照。</p>
 */
public final class TermChecker {

    private TermChecker() {}

    /**
     * <p>入力された値が高津高校現役の期生であるかを判定する。</p>
     * <p>1948年4月1日を1期生の入学日と考え、毎年の4月1日を新しい期生の入学日とする。
     * そのため、2024年3月31日には77, 78, 79期生が、2025年4月1日には78, 79, 80期生が現役の期生と判定される。</p>
     * @param termStr 期生
     * @return 入力された値が高津高校現役の期生であるか。
     */
    public static boolean checkTerm(String termStr) {
        if(termStr == null || termStr.isEmpty()) return false;
        int term;
        try {
            term = Integer.parseInt(termStr);
        } catch (NumberFormatException e) {
            return false;
        }

        int minimumTerm = getValidMinimumTerm();
        int maximumTerm = minimumTerm + 1;
        return minimumTerm <= term && term <= maximumTerm;
    }

    public static int getValidMinimumTerm() {
        int minimumTerm;
        ZonedDateTime nowTime = ZonedDateTime.of(LocalDateTime.now(), ZoneId.of("Asia/Tokyo"));
        //4月1日を新しい入学生の入学日と考え、それを境に判定する。
        ZonedDateTime entranceBorder = ZonedDateTime.of(
                nowTime.getYear(), 4, 1, 0, 0, 0, 0,
                ZoneId.of("Asia/Tokyo")
        );
        minimumTerm = nowTime.getYear() - Constants.SCHOOL_FIRST_TERM_ENTRANCE_YEAR;
        if(nowTime.isAfter(entranceBorder)) {
            minimumTerm++;
        }
        return minimumTerm;
    }
}
