package org.wickedsource.docxstamper.proxy;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.wickedsource.docxstamper.api.commentprocessor.ICommentProcessor;
import org.wickedsource.docxstamper.walk.coordinates.ParagraphCoordinates;

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
