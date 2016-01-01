package org.wickedsource.docxstamper.api;

/**
 * This exception is thrown when DocxStamper encounters an error.
 */
public class DocxStamperException extends RuntimeException {

    public DocxStamperException() {
    }

    public DocxStamperException(String message) {
        super(message);
    }

    public DocxStamperException(String message, Throwable cause) {
        super(message, cause);
    }

    public DocxStamperException(Throwable cause) {
        super(cause);
    }

    public DocxStamperException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
