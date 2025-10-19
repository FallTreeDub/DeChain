package jp.kozu_osaka.android.kozuzen.access.request;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.List;
import java.util.Map;

import jp.kozu_osaka.android.kozuzen.access.argument.Arguments;

/**
 * DeChainデータベースへの書き込みリクエストの実体。
 */
public class Request {

    protected final RequestType type;

    protected Request(RequestType type) {
        this.type = type;
    }

    public RequestType getType() {
        return this.type;
    }

    public enum RequestType {

        /**
         * 仮登録アカウントの作成のリクエスト。
         */
        REGISTER_TENTATIVE(0),

        /**
         * アプリ使用時間の記録リクエスト。
         */
        REGISTER_USAGE_DATA(2),

        /**
         *
         */
        CONFIRM_TENTATIVE_AUTHCODE(7),

        /**
         *
         */
        RECREATE_TENTATIVE_AUTHCODE(8),

        /**
         *
         */
        REQUEST_RESET_PASS(9),

        /**
         *
         */
        CONFIRM_RESET_PASS_AUTHCODE(10),

        /**
         *
         */
        RECREATE_RESET_PASS_AUTHCODE(11),

        /**
         *
         */
        GET_TENTATIVE_ACCOUNT_EXISTENCE(12),

        /**
         *
         */
        GET_REGISTERED_ACCOUNT_EXISTENCE(13),

        GET_LATEST_VERSION_CODE(14),

        GET_LATEST_VERSION_APK_LINK(15),

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
