package org.wickedsource.docxstamper.commentprocessing;

import org.wickedsource.docxstamper.DocxStamperException;
import org.wickedsource.docxstamper.walk.coordinates.ParagraphCoordinates;
import org.wickedsource.docxstamper.walk.coordinates.TableCoordinates;

public class CommentProcessingException extends DocxStamperException {

    public CommentProcessingException(String message, ParagraphCoordinates coordinates) {
        super(message + "\nCoordinates of offending commented paragraph within the document: \n" + coordinates.toString());
    }

    public CommentProcessingException(String message, TableCoordinates coordinates) {
        super(message + "\nCoordinates of offending commented table within the document: \n" + coordinates.toString());
    }

}
