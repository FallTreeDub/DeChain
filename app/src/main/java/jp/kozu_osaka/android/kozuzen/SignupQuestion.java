package jp.kozu_osaka.android.kozuzen;

import androidx.annotation.StringRes;

import java.io.Serializable;
import java.util.List;

/**
 * <p>
 *     アカウント作成時の質問への回答を集約するクラス。
 * </p>
 */
public final class SignupQuestion implements Serializable {

    private final List<Club> clubs;
    private final Gender gender;
    private final List<SNS> usuallyUseSNS;
    private final MotivationLevel motivationLevel;
    private final AgeLevel ageLevel;
    private final int motivationHour;
    private final int motivationMinute;
    private final Rule rule;
    private final DependenceLevel dependence;

    public SignupQuestion(List<Club> clubs, Gender gender,
                          List<SNS> usuallyUseSNS, MotivationLevel motivationLevel, AgeLevel ageLevel,
                          int motivationHour, int motivationMinute, Rule rule,
                          DependenceLevel dependence) {
        this.clubs = clubs;
        this.gender = gender;
        this.usuallyUseSNS = usuallyUseSNS;
        this.ageLevel = ageLevel;
        this.motivationLevel = motivationLevel;
        this.motivationHour = motivationHour;
        this.motivationMinute = motivationMinute;
        this.rule = rule;
        this.dependence = dependence;
    }

    public List<Club> getClubs() {
        return clubs;
    }

    public Gender getGender() {
        return gender;
    }

    public List<SNS> getUsuallyUseSNS() {
        return usuallyUseSNS;
    }

    public AgeLevel getAgeLevel() {
        return ageLevel;
    }

    public int getMotivationHour() {
        return motivationHour;
    }

    public int getMotivationMinute() {
        return motivationMinute;
    }

    public Rule getRule() {
        return rule;
    }

    public DependenceLevel getDependence() {
        return dependence;
    }

    public MotivationLevel getMotivationLevel() {
        return motivationLevel;
    }

    public enum Club {
        /**
         * 帰宅部。これを選択した場合は他のクラブを選択できない。
         */
        NONE(R.string.club_none),
        TRACK_AND_FIELD(R.string.club_track_and_field),
        SOCCER(R.string.club_soccer),
        RUGBY(R.string.club_rugby),
        BASEBALL(R.string.club_baseball),
        HANDBALL_MAN(R.string.club_handball_man),
        HANDBALL_WOMAN(R.string.club_handball_woman),
        HARD_TENNIS_MAN(R.string.club_hard_tennis_man),
        HARD_TENNIS_WOMAN(R.string.club_hard_tennis_woman),
        SOFT_TENNIS_WOMAN(R.string.club_soft_tennis_woman),
        WONDER(R.string.club_wonder),
        SWIMMING(R.string.club_swimming),
        KENDO(R.string.club_kendo),
        TABLE_TENNIS(R.string.club_table_tennis),
        KARATE(R.string.club_karate),
        VOLLEY_MAN(R.string.club_volley_man),
        VOLLEY_WOMAN(R.string.club_volley_woman),
        BADMINTON(R.string.club_badminton),
        BASKETBALL_MAN(R.string.club_basketball_man),
        BASKETBALL_WOMAN(R.string.club_basketball_woman),
        DRAMA(R.string.club_drama),
        SCIENCE(R.string.club_science),
        SADO(R.string.club_sado),
        BRASS_BAND(R.string.club_brass_band),
        ART(R.string.club_art),
        LIGHT_MUSIC(R.string.club_light_music),
        COMIC(R.string.club_comic),
        LITERATURE(R.string.club_literature),
        ENSEMBLE(R.string.club_ensemble),
        DANCE(R.string.club_dance),
        HOME_MADE(R.string.club_home_made),
        CREATURE(R.string.club_creature),
        CALLIGRAPHY(R.string.club_calligraphy),
        GEOGRAPHY_AND_HISTORY(R.string.club_geography_and_history),
        KARUTA(R.string.club_karuta),
        MOVIE(R.string.club_movie),
        SYOGI(R.string.club_syogi),
        ESS(R.string.club_ess);

        @StringRes
        private final int CLUB_NAME_ID;

        Club(@StringRes int clubNameId) {
            this.CLUB_NAME_ID = clubNameId;
        }

        public String getClubName() {
            return KozuZen.getInstance().getString(CLUB_NAME_ID);
        }

        public static Club from(String name) {
            for(Club c : values()) {
                if(c.getClubName().equals(name)) {
                    return c;
                }
            }
            return null;
        }

        public static String[] clubNames() {
            Club[] clubs = values();
            String[] names = new String[clubs.length];
            for(int i = 0; i < names.length; i++) {
                names[i] = clubs[i].getClubName();
            }
            return names;
        }
    }

    public enum Gender {
        MAN(R.string.text_createAccount_question_gender_man),
        WOMAN(R.string.text_createAccount_question_gender_woman),
        NO_ANSWER(R.string.text_createAccount_question_gender_no_answer);

        @StringRes
        private final int GENDER_NAME_ID;

        Gender(@StringRes int genderNameId) {
            this.GENDER_NAME_ID = genderNameId;
        }

        public String getGenderName() {
            return KozuZen.getInstance().getString(GENDER_NAME_ID);
        }

        public static Gender from(String genderName) {
            for(Gender g : values()) {
                if(g.getGenderName().equals(genderName)) {
                    return g;
                }
            }
            return null;
        }
    }

    /**
     * 普段使ってるSNSの種類。複数種類選択可能にする
     */
    public enum SNS {
        YOUTUBE(R.string.text_createAccount_question_sns_youtube),
        INSTAGRAM(R.string.text_createAccount_question_sns_instagram),
        TIKTOK(R.string.text_createAccount_question_sns_tiktok),
        FACEBOOK(R.string.text_createAccount_question_sns_facebook),
        X(R.string.text_createAccount_question_sns_x),
        CLUBHOUSE(R.string.text_createAccount_question_sns_clubhouse),
        BE_REAL(R.string.text_createAccount_question_sns_beReal),
        THREADS(R.string.text_createAccount_question_sns_threads),
        PINTEREST(R.string.text_createAccount_question_sns_pinterest),
        LINKED_IN(R.string.text_createAccount_question_sns_linkedIn),
        NICONICO(R.string.text_createAccount_question_sns_niconico),
        ICHINANA_LIVE(R.string.text_createAccount_question_sns_17live),
        POCOCHA(R.string.text_createAccount_question_sns_pococha),
        SHOWROOM(R.string.text_createAccount_question_sns_showroom),
        NONE(R.string.text_createAccount_question_sns_none),
        OTHER(R.string.text_createAccount_question_sns_other);

        @StringRes
        private final int SNS_NAME_ID;

        SNS(@StringRes int SNSNameId) {
            this.SNS_NAME_ID = SNSNameId;
        }

        public String getSNSName() {
            return KozuZen.getInstance().getString(this.SNS_NAME_ID);
        }

        public static SNS from(String snsName) {
            for(SNS s : values()) {
                if(s.getSNSName().equals(snsName)) {
                    return s;
                }
            }
            return null;
        }

        public static String[] SNSNames() {
            SNS[] sns = values();
            String[] names = new String[sns.length];
            for(int i = 0; i < names.length; i++) {
                names[i] = sns[i].getSNSName();
            }
            return names;
        }
    }

    /**
     * 普段SNS使用時間を減らそうとしているか
     */
    public enum MotivationLevel {
        LOWEST(R.string.text_createAccount_question_motivation_lowest),
        LOW(R.string.text_createAccount_question_motivation_low),
        MIDDLE(R.string.text_createAccount_question_motivation_middle),
        HIGH(R.string.text_createAccount_question_motivation_high),
        HIGHEST(R.string.text_createAccount_question_motivation_highest);

        @StringRes
        private final int LEVEL_NAME_ID;

        MotivationLevel(@StringRes int levelNameId) {
            this.LEVEL_NAME_ID = levelNameId;
        }

        public String getLevel() {
            return KozuZen.getInstance().getString(this.LEVEL_NAME_ID);
        }

        public static MotivationLevel from(String level) {
            for(MotivationLevel l : values()) {
                if(l.getLevel().equals(level)) {
                    return l;
                }
            }
            return null;
        }
    }

    public enum AgeLevel {
        NON_SELECTED(R.string.text_createAccount_question_age_nonSelected),
        BEFORE_ELEMENTARY(R.string.text_createAccount_question_age_beforeElementary),
        ELEMENTARY_FIRST(R.string.text_createAccount_question_age_elementary1),
        ELEMENTARY_SECOND(R.string.text_createAccount_question_age_elementary2),
        ELEMENTARY_THIRD(R.string.text_createAccount_question_age_elementary3),
        ELEMENTARY_FORTH(R.string.text_createAccount_question_age_elementary4),
        ELEMENTARY_FIFTH(R.string.text_createAccount_question_age_elementary5),
        ELEMENTARY_SIXTH(R.string.text_createAccount_question_age_elementary6),
        JUNIOR_HIGH_FIRST(R.string.text_createAccount_question_age_juniorHigh1),
        JUNIOR_HIGH_SECOND(R.string.text_createAccount_question_age_juniorHigh2),
        JUNIOR_HIGH_THIRD(R.string.text_createAccount_question_age_juniorHigh3),
        AFTER_JUNIOR(R.string.text_createAccount_question_age_afterJuniorHigh);

        @StringRes
        private final int LEVEL_NAME_ID;

        AgeLevel(@StringRes int levelNameId) {
            this.LEVEL_NAME_ID = levelNameId;
        }

        public String getLevelName() {
            return KozuZen.getInstance().getString(this.LEVEL_NAME_ID);
        }

        public static AgeLevel from(String level) {
            for(AgeLevel l : values()) {
                if(l.getLevelName().equals(level)) {
                    return l;
                }
            }
            return null;
        }
    }

    public enum DependenceLevel {
        LOWEST(R.string.text_createAccount_question_dependence_lowest),
        LOW(R.string.text_createAccount_question_dependence_low),
        MIDDLE(R.string.text_createAccount_question_dependence_middle),
        HIGH(R.string.text_createAccount_question_dependence_high),
        HIGHEST(R.string.text_createAccount_question_dependence_highest);

        @StringRes
        private final int LEVEL_NAME_ID;

        DependenceLevel(@StringRes int levelNameId) {
            this.LEVEL_NAME_ID = levelNameId;
        }

        public String getLevelName() {
            return KozuZen.getInstance().getString(this.LEVEL_NAME_ID);
        }

        public static DependenceLevel from(String level) {
            for(DependenceLevel l : values()) {
                if(l.getLevelName().equals(level)) {
                    return l;
                }
            }
            return null;
        }
    }

    public enum Rule {
        LOWEST(R.string.text_createAccount_question_rule_lowest),
        LOW(R.string.text_createAccount_question_rule_low),
        MIDDLE(R.string.text_createAccount_question_rule_middle),
        HIGH(R.string.text_createAccount_question_rule_high),
        HIGHEST(R.string.text_createAccount_question_rule_highest);

        @StringRes
        private final int LEVEL_NAME_ID;

        Rule(@StringRes int levelNameId) {
            this.LEVEL_NAME_ID = levelNameId;
        }

        public String getLevelName() {
            return KozuZen.getInstance().getString(this.LEVEL_NAME_ID);
        }

        public static Rule from(String level) {
            for(Rule l : values()) {
                if(l.getLevelName().equals(level)) {
                    return l;
                }
            }
            return null;
        }
    }
}
