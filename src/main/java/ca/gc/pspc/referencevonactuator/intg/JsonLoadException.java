package ca.gc.pspc.referencevonactuator.intg;

public class JsonLoadException extends RuntimeException {
    public JsonLoadException(String msg) {
        super(msg);
    }

    public JsonLoadException(Throwable cause) {
        super(cause);
    }

    public JsonLoadException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
