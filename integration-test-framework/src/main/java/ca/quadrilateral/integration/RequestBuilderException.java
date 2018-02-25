package ca.quadrilateral.integration;

public class RequestBuilderException extends Exception {
    private static final long serialVersionUID = 1L;

    public RequestBuilderException() {
        super();
    }

    public RequestBuilderException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public RequestBuilderException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public RequestBuilderException(final String message) {
        super(message);
    }

    public RequestBuilderException(final Throwable cause) {
        super(cause);
    }

}
