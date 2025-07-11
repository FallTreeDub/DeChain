package jp.kozu_osaka.android.kozuzen.internal.exception;

public class UsageDataAlreadyExistsException extends RuntimeException {
    public UsageDataAlreadyExistsException(String message) {
        super(message);
    }
}
