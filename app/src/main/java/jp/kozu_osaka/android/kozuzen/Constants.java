package jp.kozu_osaka.android.kozuzen;

/**
 * ソースコード内で使う、特に秘匿の必要のない定数を定義するクラス。
 */
public final class Constants {

    private Constants() {}

    public static final int SCHOOL_MAXIMUM_CLASS_STUDENTS = 42;
    public static final int SCHOOL_FIRST_TERM_ENTRANCE_YEAR = 1948;
    public static final int SCHOOL_FIRST_TERM_NETWORK = 73;

    /**
     * {@link SharedPreferences}のファイル指定に使用するキー群。
     */
    public static class SharedPreferences {
        public static final String PATH_BACKGROUND_REPORT = KozuZen.getInstance().getPackageName() + "_background_error" + "_preferences";
        public static final String PATH_LOGIN_STATUS = KozuZen.getInstance().getPackageName() + "_login_status" + "_preferences";
        public static final String PATH_TENTATIVE_REGISTER_STATUS = KozuZen.getInstance().getPackageName() + "tentative_register_status_" + "_preferences";
        public static final String PATH_UPDATE_PROCESS_STATUS = KozuZen.getInstance().getPackageName() + "update_process_status_" + "_preferences";
        public static final String PATH_SEND_USAGE_RECEIVER_STATUS = KozuZen.getInstance().getPackageName() + "send_usage_receiver_status_" + "_preferences";
    }

    /**
     * {@link android.content.Intent}のExtraの指定に使用するキー群。
     */
    public static class IntentExtraKey {
        public static final String ACCOUNT_MAIL = "mailAddress";
        public static final String ACCOUNT_ENCRYPTED_PASSWORD = "encryptedPassword";
        public static final String ACCOUNT_CHANGED_PASSWORD = "changedPassword";
        public static final String REPORT_BODY = "reportBody";
        public static final String SIX_AUTHORIZATION_CODE_TYPE = "authorizationCodeType";
        public static final String RECEIVER_EXIT_CODE = "receiverExitCode";
        public static final String RECEIVER_EXIT_SESSION_ID = "receiverExit_sessionID";
        public static final String RECEIVER_EXIT_APK_PATH = "receiverExit_apkPath";
        public static final String UPDATE_INSTALLED_APK_PATH = "installedAPKPath";
        public static final String TUTORIAL_CONTENT_ID = "tutorialContentID";
    }
}
