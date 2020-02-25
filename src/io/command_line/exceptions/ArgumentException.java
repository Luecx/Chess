package io.command_line.exceptions;

/**
 * Created by finne on 27.01.2018.
 */
public class ArgumentException extends Exception{

    public ArgumentException() {
    }

    public ArgumentException(String message) {
        super(message);
    }

    public ArgumentException(String message, Throwable cause) {
        super(message, cause);
    }

    public ArgumentException(Throwable cause) {
        super(cause);
    }

    public ArgumentException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
