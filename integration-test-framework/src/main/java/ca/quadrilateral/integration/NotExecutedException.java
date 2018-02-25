package ca.quadrilateral.integration;

public class NotExecutedException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public NotExecutedException() {
        super();
    }

    public NotExecutedException(
            final String message, 
            final Throwable cause, 
            final boolean enableSuppression, 
            final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public NotExecutedException(
            final String message, 
            final Throwable cause) {
        super(message, cause);
    }

    public NotExecutedException(
            final String message) {
        super(message);
    }

    public NotExecutedException(
            final Throwable cause) {
        super(cause);
    }

}
