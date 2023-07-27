package org.wickedsource.docxstamper.api;

/**
 * This exception is thrown when DocxStamper encounters an error.
 *
 * @author joseph
 * @version $Id: $Id
 */
public class DocxStamperException extends RuntimeException {

    /**
     * <p>Constructor for DocxStamperException.</p>
     *
     * @param message a message describing the error
     */
    public DocxStamperException(String message) {
        super(message);
    }

    /**
     * <p>Constructor for DocxStamperException.</p>
     *
     * @param message a message describing the error
     * @param cause   the cause of the error
     */
    public DocxStamperException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * <p>Constructor for DocxStamperException.</p>
     *
     * @param cause the cause of the error
     */
    public DocxStamperException(Throwable cause) {
        super(cause);
    }
}
