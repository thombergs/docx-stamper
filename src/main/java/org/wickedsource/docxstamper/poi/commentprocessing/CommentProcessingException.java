package org.wickedsource.docxstamper.poi.commentprocessing;

import org.wickedsource.docxstamper.DocxStamperException;
import org.wickedsource.docxstamper.poi.coordinates.ParagraphCoordinates;
import org.wickedsource.docxstamper.poi.coordinates.TableCoordinates;

public class CommentProcessingException extends DocxStamperException {

    public CommentProcessingException(String message, ParagraphCoordinates coordinates) {
        super(message + "\nCoordinates of offending commented paragraph within the document: \n" + coordinates.toString());
    }

    public CommentProcessingException(String message, TableCoordinates coordinates) {
        super(message + "\nCoordinates of offending commented table within the document: \n" + coordinates.toString());
    }

}
