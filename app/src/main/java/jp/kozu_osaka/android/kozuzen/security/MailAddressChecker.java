package jp.kozu_osaka.android.kozuzen.security;

import java.util.Locale;

import jp.kozu_osaka.android.kozuzen.Constants;

/**
 * メールアドレスとして入力された文字列が学校アカウントの物かを判定する。
 */
public final class MailAddressChecker {

    private MailAddressChecker() {}

    /**
     * {@code str}として渡された文字列が高津高校のGoogle学校アカウントのメールアドレスであるかを判定する。
     * @param str メールアドレスとして設定する文字列。
     * @return 高津高校のGoogle学校アカウントのメールアドレスであるか。
     */
    public static boolean checkMailAddress(String str) {
        if(str == null || str.isEmpty()) {
            return false;
        }
        int mailTermNumMinimum = TermChecker.getValidMinimumTerm() - Constants.SCHOOL_FIRST_TERM_NETWORK;
        int mailTermNumMaximum = mailTermNumMinimum + 1;
        if(!str.matches(String.format(Locale.JAPAN, Secrets.SCHOOL_MAIL_REGEX,
                Secrets.SCHOOL_MAIL_NUMBER, mailTermNumMinimum,
                mailTermNumMaximum, Secrets.SCHOOL_MAIL_DOMAIN))) {
            return false;
        }
        String userName = str.split("@" + Secrets.SCHOOL_MAIL_DOMAIN)[0];
        try {
            int studentNumber = Integer.parseInt(userName.substring(userName.length() - 2));
            if(!(1 <= studentNumber && studentNumber <= Constants.SCHOOL_MAXIMUM_CLASS_STUDENTS)) {
                return false;
            }
        } catch(NumberFormatException e) {
            return false;
        }
        return userName.substring(userName.length() - 6, userName.length() - 3)
                .equals(userName.substring(userName.length() - 3)); //クラス番号+出席番号の2回の繰り返しであるか
    }
}
