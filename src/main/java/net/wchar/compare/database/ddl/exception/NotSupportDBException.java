package net.wchar.compare.database.ddl.exception;

public class NotSupportDBException extends RuntimeException{
    public NotSupportDBException() {
    }

    public NotSupportDBException(String message) {
        super(message);
    }

    public NotSupportDBException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotSupportDBException(Throwable cause) {
        super(cause);
    }

    public NotSupportDBException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
