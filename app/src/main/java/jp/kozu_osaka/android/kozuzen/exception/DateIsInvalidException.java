package jp.kozu_osaka.android.kozuzen.exception;

/**
 * 内部jsonに一日のアプリ使用時間を記録する際、その日がjsonの対象とする年月にのっとっていない場合に
 * スローされる。
 */
public final class DateIsInvalidException extends RuntimeException {
    public DateIsInvalidException(String message) {
        super(message);
    }
}
