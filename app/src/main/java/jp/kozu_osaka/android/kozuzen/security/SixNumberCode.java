package jp.kozu_osaka.android.kozuzen.security;

import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.regex.Pattern;

import jp.kozu_osaka.android.kozuzen.access.request.Request;

public final class SixNumberCode implements Serializable {

    private final String code;
    private final CodeType type;

    private SixNumberCode(String code, CodeType type) {
        this.code = code;
        this.type = type;
    }

    /**
     * {@code String}の形式の6桁コードから、コード使用場所である{@link CodeType}を格納した{@link SixNumberCode}
     * インスタンスを生成する。
     * @param code {@code String}形式の6桁コード
     * @param type 6桁コードの認証先。
     * @return 生成されたインスタンス。{@code code}が6桁の数字でない場合はnullが返される。
     */
    @Nullable
    public static SixNumberCode toInstance(String code, CodeType type) {
        if(!Pattern.matches("[0-9]{6}", code)) return null;
        return new SixNumberCode(code, type);
    }

    public String getCode() {
        return this.code;
    }

    public CodeType getType() {
        return this.type;
    }

    /**
     * 6桁コード認証の使いどころを決定する。
     * 6桁コード認証は、パスワードリセットやアカウント登録に用いるため、
     * 複数場面での活用が不可欠。
     */
    public enum CodeType {

        FOR_CREATE_ACCOUNT(Request.RequestType.RECREATE_TENTATIVE_AUTHCODE, Request.RequestType.CONFIRM_TENTATIVE_AUTHCODE),

        FOR_PASSWORD_RESET(
                Request.RequestType.RECREATE_RESET_PASS_AUTHCODE, Request.RequestType.CONFIRM_RESET_PASS_AUTHCODE);

        /**
         * 認証コード再生成の際に使う{@link jp.kozu_osaka.android.kozuzen.access.request.Request.RequestType]}。
         */
        private final Request.RequestType RECREATE_REQUEST_TYPE;

        /**
         * 認証コードの一致をSpreadSheetへ知らせる時の{@link jp.kozu_osaka.android.kozuzen.access.request.Request.RequestType}。
         */
        private final Request.RequestType CONFIRM_AUTH_REQUEST_TYPE;

        /**
         * {@code CodeType}のコンストラクタ。
         */
        CodeType(Request.RequestType recreateRequestType, Request.RequestType confirmAuthRequestType) {
            this.RECREATE_REQUEST_TYPE = recreateRequestType;
            this.CONFIRM_AUTH_REQUEST_TYPE = confirmAuthRequestType;
        }

        public Request.RequestType getRecreateRequestType() {
            return this.RECREATE_REQUEST_TYPE;
        }

        public Request.RequestType getConfirmAuthRequestType() {
            return this.CONFIRM_AUTH_REQUEST_TYPE;
        }
    }
}
