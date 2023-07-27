package org.wickedsource.docxstamper.api;

/**
 * This exception is thrown if an expression could not be processed by any comment processor.
 *
 * @author joseph
 * @version $Id: $Id
 */
public class UnresolvedExpressionException extends DocxStamperException {
    /**
     * <p>Constructor for UnresolvedExpressionException.</p>
     *
     * @param expression the expression that could not be processed.
     * @param cause      the root cause for this exception
     */
    public UnresolvedExpressionException(String expression, Throwable cause) {
        super(String.format("The following expression could not be processed by any comment processor: %s", expression), cause);
    }
}
