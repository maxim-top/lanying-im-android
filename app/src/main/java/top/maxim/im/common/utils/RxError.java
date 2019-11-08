
package top.maxim.im.common.utils;

public class RxError extends Exception {

    public static final int ERROR_UNKNOWN = -1;

    public static final int ERROR_TYPE_DATA = 1;

    public static final int ERROR_TYPE_COMMON = 2;

    public int errorCode = -1;

    public String message;

    public int type = 2;

    public RxError() {
    }

    private RxError(String detailMessage) {
        super(detailMessage);
    }

    private RxError(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    private RxError(Throwable throwable) {
        super(throwable);
    }

    public static RxError create(int type, int errorCode) {
        return create(type, errorCode, "", (Throwable)null);
    }

    public static RxError create(int type, int errorCode, String detailMessage) {
        return create(type, errorCode, detailMessage, (Throwable)null);
    }

    public static RxError create(int type, int errorCode, Throwable throwable) {
        return create(type, errorCode, "", throwable);
    }

    public static RxError create(int type, int errorCode, String detailMessage,
            Throwable throwable) {
        RxError e = new RxError(detailMessage, throwable);
        e.errorCode = errorCode;
        e.type = type;
        e.message = detailMessage;
        return e;
    }
}
