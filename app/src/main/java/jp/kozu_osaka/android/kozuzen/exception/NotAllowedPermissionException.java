package jp.kozu_osaka.android.kozuzen.exception;

/**
 * アプリ内の動作で、Android上の必要な権限が許可されていないときにスローされる。
 */
public class NotAllowedPermissionException extends Exception {

    private final String notAllowedPermission;

    public NotAllowedPermissionException(String message, String notAllowedPermission) {
        super(message);
        this.notAllowedPermission = notAllowedPermission;
    }

    public String getNotAllowedPermission() {
        return this.notAllowedPermission;
    }
}
