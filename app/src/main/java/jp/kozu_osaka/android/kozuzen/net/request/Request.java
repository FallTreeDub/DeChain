package jp.kozu_osaka.android.kozuzen.net.request;

/**
 * DeChainデータベースとの通信のリクエスト内容。
 */
public class Request {

    protected final RequestType type;

    /**
     * 汎用エラーコード群
     */
    public static final int RESPONSE_CODE_NO_ERROR = 0;
    public static final int RESPONSE_CODE_NO_ERROR_WITH_MESSAGE = -1;
    public static final int RESPONSE_CODE_ARGUMENT_NULL = 310;
    public static final int RESPONSE_CODE_ARGUMENT_NON_SIGNATURES = 311;
    public static final int RESPONSE_CODE_INVALID_OPERATION_ID = 312;
    public static final int RESPONSE_CODE_NOT_FOUND_TENTATIVE = 120;
    public static final int RESPONSE_CODE_NOT_FOUND_REGED = 220;
    public static final int RESPONSE_CODE_ALREADY_REGED_TENTATIVE = 110;
    public static final int RESPONSE_CODE_ALREADY_REGED_REGED = 210;

    protected Request(RequestType type) {
        this.type = type;
    }

    /**
     * どのような通信をするかを示す、{@link RequestType}を返す。
     */
    public RequestType getType() {
        return this.type;
    }

    /**
     * DeChainデータベースとの通信で、どのような内容で通信するかを決定するID。
     */
    public enum RequestType {

        /**
         * 仮登録アカウント作成リクエスト。
         */
        REGISTER_TENTATIVE(0),

        /**
         * アプリ使用時間の送信リクエスト。
         */
        REGISTER_USAGE_DATA(2),

        /**
         * 仮登録の6桁認証のコードを送信するリクエスト。
         */
        CONFIRM_TENTATIVE_AUTHCODE(7),

        /**
         * 仮登録の6桁認証のコードの再作成を要求するリクエスト。
         */
        RECREATE_TENTATIVE_AUTHCODE(8),

        /**
         * 本登録アカウントのパスワードのリセットを要求するリクエスト。
         */
        REQUEST_RESET_PASS(9),

        /**
         * 本登録アカウントのパスワードリセットの6桁認証コードを送信するリクエスト。
         */
        CONFIRM_RESET_PASS_AUTHCODE(10),

        /**
         * 本登録アカウントのパスワードリセットの6桁認証コードの再作成を要求するリクエスト。
         */
        RECREATE_RESET_PASS_AUTHCODE(11),

        /**
         * 特定の仮登録アカウントが存在するかを確かめるリクエスト。
         */
        GET_TENTATIVE_ACCOUNT_EXISTENCE(12),

        /**
         * 特定の本登録アカウントが存在するかを確かめるリクエスト。
         */
        GET_REGISTERED_ACCOUNT_EXISTENCE(13),

        /**
         * DeChainアプリの最新のバージョンコードを要求するリクエスト。
         */
        GET_LATEST_VERSION_CODE(14),

        /**
         * DeChainアプリの最新バージョンのAPKのリンクを要求するリクエスト。
         */
        GET_LATEST_VERSION_APK_LINK(15),

        /**
         * 被験者全体の一日分の利用時間の平均値を要求するリクエスト。
         */
        GET_AVERAGE_OF_USAGE_ONE_DAY(16);

        private final int REQUEST_CODE;

        RequestType(int requestCode) {
            this.REQUEST_CODE = requestCode;
        }

        public int getRequestCode() {
            return this.REQUEST_CODE;
        }
    }
}
