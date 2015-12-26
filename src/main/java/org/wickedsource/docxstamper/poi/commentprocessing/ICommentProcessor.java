package org.wickedsource.docxstamper.poi.commentprocessing;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.wickedsource.docxstamper.poi.coordinates.ParagraphCoordinates;

public interface ICommentProcessor {

    public void commitChanges(XWPFDocument document);

    public void setCurrentParagraphCoordinates(ParagraphCoordinates coordinates);

}
