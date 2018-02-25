package ca.quadrilateral.integration;

public class RequestException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public RequestException() {
        super();
    }

    public RequestException(
            final String message, 
            final Throwable cause, 
            final boolean enableSuppression, 
            final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public RequestException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public RequestException(final String message) {
        super(message);
    }

    public RequestException(final Throwable cause) {
        super(cause);
    }

}
