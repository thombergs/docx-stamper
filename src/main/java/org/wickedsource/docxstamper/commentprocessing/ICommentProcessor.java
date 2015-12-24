package org.wickedsource.docxstamper.commentprocessing;

import org.apache.poi.xwpf.usermodel.*;
import org.wickedsource.docxstamper.walk.coordinates.ParagraphCoordinates;

public interface ICommentProcessor {

    public void commitChanges(XWPFDocument document);

    public void setCurrentParagraphCoordinates(ParagraphCoordinates coordinates);

}
