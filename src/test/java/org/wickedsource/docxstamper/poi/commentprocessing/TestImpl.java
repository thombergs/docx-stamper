package org.wickedsource.docxstamper.poi.commentprocessing;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.wickedsource.docxstamper.poi.coordinates.ParagraphCoordinates;

public class TestImpl implements ITestInterface, ICommentProcessor {

    @Override
    public String returnString(String string) {
        return string;
    }

    @Override
    public void commitChanges(XWPFDocument document) {

    }

    @Override
    public void setCurrentParagraphCoordinates(ParagraphCoordinates coordinates) {

    }

}
