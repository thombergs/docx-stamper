package org.wickedsource.docxstamper.docx4j.commentprocessing;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.wickedsource.docxstamper.docx4j.processor.ICommentProcessor;
import org.wickedsource.docxstamper.docx4j.walk.coordinates.ParagraphCoordinates;

public class TestImpl implements ITestInterface, ICommentProcessor {

    @Override
    public String returnString(String string) {
        return string;
    }

    @Override
    public void commitChanges(WordprocessingMLPackage document) {

    }

    @Override
    public void setCurrentParagraphCoordinates(ParagraphCoordinates coordinates) {

    }

}
