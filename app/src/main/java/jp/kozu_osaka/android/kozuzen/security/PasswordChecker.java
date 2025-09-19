package jp.kozu_osaka.android.kozuzen.security;

/**
 * <p>パスワードの堅牢性を検査する。</p>
 * <p>パスワードの指定条件は以下のとおりである。</p>
 * <li>半角英数字、記号のみで構成されている</li>
 * <li>10文字以上、また20文字以下である</li>
 * <li>半角アルファベット大文字と小文字の両方、数字、記号をそれぞれ2文字以上含める</li>
 * <li>使用可能な記号は次の通り: -!#$%&()*,.:;?@[]^_{|}~+<=></li>
 * パスワード安全基準は総務省のHPより<br>
 * <a href="https://www.soumu.go.jp/main_sosiki/cybersecurity/kokumin/security/end_user/general/01/">総務省「推測できる簡単なパスワードを利用しないようにしよう」</a>
 */
public final class PasswordChecker {

    private PasswordChecker() {}

    /**
     * <p>{@code str}として渡された文字列がパスワードとして堅牢かを判定する。</p>
     * <p>堅牢性の判断基準は{@link PasswordChecker}のドキュメントを参照。</p>
     * @param str パスワードとして設定する予定の文字列。
     * @return パスワードが堅牢であるか。
     */
    public static SafetyStatus checkPassword(String str) {
        SafetyStatus status = new SafetyStatus();
        if(str == null || str.isEmpty()) {
            status.setMeetsMinLenOfAlnumsAndSymbols(false);
            status.setRangeInLimit(false);
            status.setOnlyAlnumsAndSymbols(false);
            return status;
        }
        if(!(10 <= str.length() && str.length() <= 20)) {
            status.setRangeInLimit(false);
        }

        if(!str.matches("[0-9a-zA-Z\\-!#$%&()*,.:;?@\\[\\]^_{|}~+<=>]+")) { //半角英数字、記号で構成されていない場合
            status.setOnlyAlnumsAndSymbols(false);
        }

        String numberRegex = "[0-9]";
        String smallAlphabetRegex = "[a-z]";
        String bigAlphabetRegex = "[A-Z]";
        String symbolRegex = "[\\-!#$%&()*,.:;?@\\[\\]^_{|}~+<=>]";

        byte numberMatch = 0;
        byte bigAlphabetMatch = 0;
        byte smallAlphabetMatch = 0;
        byte symbolMatch = 0;

        for(char c : str.toCharArray()) {
            if(String.valueOf(c).matches(numberRegex)) numberMatch++;
            if(String.valueOf(c).matches(smallAlphabetRegex)) smallAlphabetMatch++;
            if(String.valueOf(c).matches(bigAlphabetRegex)) bigAlphabetMatch++;
            if(String.valueOf(c).matches(symbolRegex)) symbolMatch++;
        }
        if(!(numberMatch >= 2 && bigAlphabetMatch >= 2 &&
                smallAlphabetMatch >= 2 && symbolMatch >= 2)) {
            status.setMeetsMinLenOfAlnumsAndSymbols(false);
        }
        return status;
    }

    /**
     * <p>パスワードの堅牢性を示す。</p>
     * <p>堅牢性の判断基準は{@link PasswordChecker}のドキュメントを参照。</p>
     */
    public static class SafetyStatus {

        /**
         * 文字数制限を守っているか。
         */
        private boolean isRangeInLimit;

        /**
         * 半角英数字、指定記号のみで構成されているか。
         */
        private boolean isOnlyAlnumsAndSymbols;

        /**
         * 半角英数字、指定記号をそれぞれ2文字以上含んでいるか。
         */
        private boolean meetsMinLenOfAlnumsAndSymbols;

        public SafetyStatus() {
            this.meetsMinLenOfAlnumsAndSymbols = true;
            this.isRangeInLimit = true;
            this.isOnlyAlnumsAndSymbols = true;
        }

        public void setRangeInLimit(boolean rangeInLimit) {
            isRangeInLimit = rangeInLimit;
        }

        public void setOnlyAlnumsAndSymbols(boolean onlyAlnumsAndSymbols) {
            this.isOnlyAlnumsAndSymbols = onlyAlnumsAndSymbols;
        }

        public void setMeetsMinLenOfAlnumsAndSymbols(boolean meetsMinLenOfAlnumsAndSymbols) {
            this.meetsMinLenOfAlnumsAndSymbols = meetsMinLenOfAlnumsAndSymbols;
        }

        /**
         * @return 文字数制限を守っているか。
         */
        public boolean isRangeInLimit() {
            return isRangeInLimit;
        }

        /**
         * @return 半角英数字、指定記号のみで構成されているか。
         */
        public boolean isOnlyAlnumsAndSymbols() {
            return isOnlyAlnumsAndSymbols;
        }

        /**
         * @return 半角英数字、指定記号をそれぞれ2文字以上含んでいるか。
         */
        public boolean meetsMinLenOfAlnumsAndSymbols() {
            return meetsMinLenOfAlnumsAndSymbols;
        }
    }
}
