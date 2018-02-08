package ca.gc.pspc.referencevonactuator.intg;

public class JsonValidateException extends Exception {
    public JsonValidateException(String msg) {
        super(msg);
    }

    public JsonValidateException(Throwable e) {
        super("JSON validation error", e);
    }
}
