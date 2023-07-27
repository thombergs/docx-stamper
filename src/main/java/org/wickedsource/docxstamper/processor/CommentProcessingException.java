package org.wickedsource.docxstamper.processor;

import org.docx4j.wml.P;
import org.wickedsource.docxstamper.api.DocxStamperException;

import static java.lang.String.format;
import static org.docx4j.TextUtils.getText;

/**
 * Thrown when an error occurs while processing a comment in the docx template.
 *
 * @author joseph
 * @version $Id: $Id
 */
public class CommentProcessingException extends DocxStamperException {

    /**
     * <p>Constructor for CommentProcessingException.</p>
     *
     * @param message   the error message
     * @param paragraph the paragraph containing the comment that caused the error
     */
    public CommentProcessingException(String message, P paragraph) {
        super(format("%s : %s", message, getText(paragraph)));
    }

}
