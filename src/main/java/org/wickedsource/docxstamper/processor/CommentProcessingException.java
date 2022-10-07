package org.wickedsource.docxstamper.processor;

import org.docx4j.TextUtils;
import org.docx4j.wml.P;
import org.wickedsource.docxstamper.api.DocxStamperException;

import static java.text.MessageFormat.format;

public class CommentProcessingException extends DocxStamperException {
    private static final String PATTERN = "{0}\n" +
            "Coordinates of offending commented paragraph within the document: \n" +
            "{1}";

    public CommentProcessingException(String message, P paragraph) {
        this(message, TextUtils.getText(paragraph));
    }

    private CommentProcessingException(String message, String paragraphContent) {
        this(format(PATTERN, message, paragraphContent));
    }

    private CommentProcessingException(String message) {
        super(message);
    }
}
