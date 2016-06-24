package io.github.pivopil.share.throwble;

import org.springframework.http.HttpStatus;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created on 18.04.16.
 */
public class ExceptionAdapter extends RuntimeException {

    public final HttpStatus httpStatus;

    public final ErrorType errorType;

    public final String message;

    public final int errorCode;

    public Exception originalException;

    private final String stackTrace;

    public ExceptionAdapter(String message, CustomError customError, HttpStatus httpStatus) {
        this(message, customError.getCode(), customError.getCategory(), httpStatus, null);
    }

    public ExceptionAdapter(String message, CustomError customError, HttpStatus httpStatus, Exception cause) {
        this(message, customError.getCode(), customError.getCategory(), httpStatus, cause);
    }


    public ExceptionAdapter(Exception e) {
        this(e.getMessage(), 0, null, null, e);
    }

    public ExceptionAdapter(String message, int errorCode, ErrorType errorType, HttpStatus httpStatus, Exception e) {
        super(e.toString());
        this.httpStatus = httpStatus;
        this.errorType = errorType;
        this.errorCode = errorCode;
        this.message = message;
        originalException = e;
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        stackTrace = sw.toString();
    }

    public void printStackTrace() {
        printStackTrace(System.err);
    }

    public void printStackTrace(java.io.PrintStream s) {
        synchronized (s) {
            s.print(getClass().getName() + ": ");
            s.print(stackTrace);
        }
    }

    public void printStackTrace(PrintWriter s) {
        synchronized (s) {
            s.print(getClass().getName() + ": ");
            s.print(stackTrace);
        }
    }

    public Throwable rethrow() {
        return originalException;
    }
}