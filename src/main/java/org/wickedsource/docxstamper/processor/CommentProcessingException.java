package org.wickedsource.docxstamper.processor;

import org.docx4j.wml.P;
import org.wickedsource.docxstamper.api.DocxStamperException;
import org.wickedsource.docxstamper.api.coordinates.ParagraphCoordinates;
import org.wickedsource.docxstamper.api.coordinates.TableCoordinates;

public class CommentProcessingException extends DocxStamperException {

    public CommentProcessingException(String message, P paragraph) {
        super(message + "\nCoordinates of offending commented paragraph within the document: \n" + paragraph.toString());
    }
}
