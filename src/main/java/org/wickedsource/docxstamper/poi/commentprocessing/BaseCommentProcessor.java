package org.wickedsource.docxstamper.poi.commentprocessing;

import org.wickedsource.docxstamper.poi.coordinates.ParagraphCoordinates;

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
