package jp.kozu_osaka.android.kozuzen.access.argument;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import jp.kozu_osaka.android.kozuzen.SignupQuestion;
import jp.kozu_osaka.android.kozuzen.security.HashedString;

public class TentativeRegisterArguments extends Arguments {

    /**
     * メールアドレスのキー。
     */
    private static final String KEY_MAIL = "mail";

    /**
     * パスワードのキー。
     */
    private static final String KEY_PASS = "pass";

    /**
     * 所属クラブのキー。
     */
    private static final String KEY_CLUB = "club";

    /**
     * 期生のキー。
     */
    private static final String KEY_TERM = "term";

    /**
     * 性別のキー。
     */
    private static final String KEY_GENDER = "gender";

    /**
     * 普段使っているSNSのキー。
     */
    private static final String KEY_SNS_USUALLY = "snsUsually";

    /**
     * SNS利用時間を減らしたい意欲のキー。
     */
    private static final String KEY_DESIRE_TO_DEMINISH = "desireToDeminish";

    /**
     * SNSの利用時間を何時間何分減らしたいかのキー。
     */
    private static final String KEY_TIME_DEMINISHED = "timeDeminished";

    /**
     * SNSを使う上でルールを決めているかのキー。
     */
    private static final String KEY_IS_THERE_RULE = "isThereRules";

    /**
     * 依存自覚度のキー。
     */
    private static final String KEY_NOTICE = "notice";

    /**
     * いつスマホを手に入れたかのキー。
     */
    private static final String KEY_WHEN_GET_PHONE = "whenGetPhone";

    public TentativeRegisterArguments(String mail, HashedString pass, SignupQuestion signupQuestion) {
        super(Map.ofEntries(
                Map.entry(KEY_MAIL, mail),
                Map.entry(KEY_PASS, pass.toString()),
                Map.entry(KEY_CLUB, generateClubStringChain(signupQuestion.getClubs())),
                Map.entry(KEY_TERM, String.valueOf(signupQuestion.getTerm())),
                Map.entry(KEY_GENDER, signupQuestion.getGender().getGenderName()),
                Map.entry(KEY_SNS_USUALLY, generateSNSStringChain(signupQuestion.getUsuallyUseSNS())),
                Map.entry(KEY_DESIRE_TO_DEMINISH, signupQuestion.getMotivationLevel().getLevel()),
                Map.entry(KEY_TIME_DEMINISHED, String.format(Locale.JAPAN, "%d時間%d分", signupQuestion.getMotivationHour(), signupQuestion.getMotivationMinute())),
                Map.entry(KEY_IS_THERE_RULE, signupQuestion.getRule().getLevelName()),
                Map.entry(KEY_NOTICE, signupQuestion.getDependence().getLevelName()),
                Map.entry(KEY_WHEN_GET_PHONE, signupQuestion.getAgeLevel().getLevelName())
        ));
    }

    private static String generateSNSStringChain(List<SignupQuestion.SNS> sns) {
        StringBuilder builder = new StringBuilder();
        for(SignupQuestion.SNS s : sns) {
            builder.append(s.getSNSName()).append(",");
        }
        return builder.toString();
    }

    private static String generateClubStringChain(List<SignupQuestion.Club> clubs) {
        StringBuilder builder = new StringBuilder();
        for(SignupQuestion.Club c : clubs) {
            builder.append(c.getClubName()).append(",");
        }
        return builder.toString();
    }
}
