package org.wickedsource.docxstamper.processor;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.wickedsource.docxstamper.walk.coordinates.ParagraphCoordinates;

public interface ICommentProcessor {

    public void commitChanges(WordprocessingMLPackage document);

    public void setCurrentParagraphCoordinates(ParagraphCoordinates coordinates);

}
