package jp.kozu_osaka.android.kozuzen;

import android.os.Build;

import java.nio.file.Path;

public final class Constants {

    private Constants() {}

    public static final int SCHOOL_MAXIMUM_CLASS_STUDENTS = 42;
    public static final int SCHOOL_FIRST_TERM_ENTRANCE_YEAR = 1948;
    public static final int SCHOOL_FIRST_TERM_NETWORK = 73;

    public static class SharedPreferences {
        public static final String PATH_BACKGROUND_REPORT = KozuZen.getInstance().getPackageName() + "_background_error" + "_preferences";
        public static final String PATH_LOGIN_STATUS = KozuZen.getInstance().getPackageName() + "_login_status" + "_preferences";
        public static final String PATH_TENTATIVE_REGISTER_STATUS = KozuZen.getInstance().getPackageName() + "tentative_register_status_" + "_preferences";
    }

    public static class IntentExtraKey {
        public static final String ACCOUNT_MAIL = "mailAddress";
        public static final String ACCOUNT_ENCRYPTED_PASSWORD = "encryptedPassword";
        public static final String REPORT_BODY = "reportBody";
        public static final String SIX_AUTHORIZATION_CODE_TYPE = "authorizationCodeType";
    }
}
