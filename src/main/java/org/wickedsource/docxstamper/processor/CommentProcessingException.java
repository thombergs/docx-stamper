package org.wickedsource.docxstamper.processor;

import org.docx4j.wml.P;
import org.wickedsource.docxstamper.api.DocxStamperException;

import static java.lang.String.format;
import static org.docx4j.TextUtils.getText;

public class CommentProcessingException extends DocxStamperException {

    public CommentProcessingException(String message, P paragraph) {
        super(format("%s : %s", message, getText(paragraph)));
    }

}
