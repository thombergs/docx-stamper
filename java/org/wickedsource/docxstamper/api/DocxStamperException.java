package org.wickedsource.docxstamper.api;

/**
 * This exception is thrown when DocxStamper encounters an error.
 */
public class DocxStamperException extends RuntimeException {

    public DocxStamperException(String message) {
        super(message);
    }

    public DocxStamperException(String message, Throwable cause) {
        super(message, cause);
    }

    public DocxStamperException(Throwable cause) {
        super(cause);
    }

}
