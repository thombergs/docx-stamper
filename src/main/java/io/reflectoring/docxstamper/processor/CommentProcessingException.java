package io.reflectoring.docxstamper.processor;

import io.reflectoring.docxstamper.api.DocxStamperException;
import io.reflectoring.docxstamper.api.coordinates.ParagraphCoordinates;
import io.reflectoring.docxstamper.api.coordinates.TableCoordinates;

public class CommentProcessingException extends DocxStamperException {

    public CommentProcessingException(String message, ParagraphCoordinates coordinates) {
        super(message + "\nCoordinates of offending commented paragraph within the document: \n" + coordinates.toString());
    }

    public CommentProcessingException(String message, TableCoordinates coordinates) {
        super(message + "\nCoordinates of offending commented table within the document: \n" + coordinates.toString());
    }

}
