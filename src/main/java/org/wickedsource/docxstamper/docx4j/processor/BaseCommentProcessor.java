package org.wickedsource.docxstamper.docx4j.processor;


import org.wickedsource.docxstamper.docx4j.walk.coordinates.ParagraphCoordinates;

public abstract class BaseCommentProcessor implements ICommentProcessor {

    private ParagraphCoordinates currentParagraphCoordinates;

    @Override
    public void setCurrentParagraphCoordinates(ParagraphCoordinates coordinates) {
        this.currentParagraphCoordinates = coordinates;
    }

    public ParagraphCoordinates getCurrentParagraphCoordinates() {
        return currentParagraphCoordinates;
    }
}
