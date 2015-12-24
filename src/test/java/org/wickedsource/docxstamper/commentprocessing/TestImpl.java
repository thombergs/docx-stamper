package org.wickedsource.docxstamper.commentprocessing;

import org.apache.poi.xwpf.usermodel.*;
import org.wickedsource.docxstamper.walk.coordinates.ParagraphCoordinates;

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
