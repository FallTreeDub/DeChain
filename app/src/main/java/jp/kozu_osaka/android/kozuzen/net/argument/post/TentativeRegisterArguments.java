package jp.kozu_osaka.android.kozuzen.net.argument.post;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jp.kozu_osaka.android.kozuzen.SignupQuestion;
import jp.kozu_osaka.android.kozuzen.security.HashedString;

public final class TentativeRegisterArguments extends PostArguments {

    /**
     * メールアドレスのキー。
     */
    private static final String KEY_MAIL = "mail";

    /**
     * 被験者の学年のキー。
     */
    private static final String KEY_GRADE = "grade";

    /**
     * 被験者のクラス番号のキー。
     */
    private static final String KEY_CLASS = "class";

    /**
     * 被験者の出席番号。
     */
    private static final String KEY_NUMBER = "number";

    /**
     * パスワードのキー。
     */
    private static final String KEY_PASS = "pass";

    /**
     * 所属クラブのキー。
     */
    private static final String KEY_CLUB = "club";

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

    public TentativeRegisterArguments(String mail, HashedString pass,
                                      String grade, String clazz, String number,
                                      SignupQuestion signupQuestion) {
        super(Map.ofEntries(
                Map.entry(KEY_MAIL, Collections.singletonList(mail)),
                Map.entry(KEY_GRADE, Collections.singletonList(grade)),
                Map.entry(KEY_CLASS, Collections.singletonList(clazz)),
                Map.entry(KEY_NUMBER, Collections.singletonList(number)),
                Map.entry(KEY_PASS, Collections.singletonList(pass.toString())),
                Map.entry(KEY_CLUB, Collections.singletonList(generateClubNameList(signupQuestion.getClubs()))),
                Map.entry(KEY_GENDER, Collections.singletonList(signupQuestion.getGender().getGenderName())),
                Map.entry(KEY_SNS_USUALLY, Collections.singletonList(generateSNSNameList(signupQuestion.getUsuallyUseSNS()))),
                Map.entry(KEY_DESIRE_TO_DEMINISH, Collections.singletonList(signupQuestion.getMotivationLevel().getLevel())),
                Map.entry(KEY_TIME_DEMINISHED, Collections.singletonList(String.format(Locale.JAPAN, "%d時間%d分", signupQuestion.getMotivationHour(), signupQuestion.getMotivationMinute()))),
                Map.entry(KEY_IS_THERE_RULE, Collections.singletonList(signupQuestion.getRule().getLevelName())),
                Map.entry(KEY_NOTICE, Collections.singletonList(signupQuestion.getDependence().getLevelName())),
                Map.entry(KEY_WHEN_GET_PHONE, Collections.singletonList(signupQuestion.getAgeLevel().getLevelName()))
        ));
    }

    private static String generateSNSNameList(List<SignupQuestion.SNS> sns) {
        StringBuilder b = new StringBuilder();
        for(SignupQuestion.SNS s : sns) {
            b.append(s.getSNSName()).append(",");
        }
        return b.toString();
    }

    private static String generateClubNameList(List<SignupQuestion.Club> clubs) {
        StringBuilder b = new StringBuilder();
        for(SignupQuestion.Club c : clubs) {
            b.append(c.getClubName()).append(",");
        }
        return b.toString();
    }
}
