package org.wickedsource.docxstamper.docx4j.processor;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.wickedsource.docxstamper.docx4j.walk.coordinates.ParagraphCoordinates;

public interface ICommentProcessor {

    public void commitChanges(WordprocessingMLPackage document);

    public void setCurrentParagraphCoordinates(ParagraphCoordinates coordinates);

}
