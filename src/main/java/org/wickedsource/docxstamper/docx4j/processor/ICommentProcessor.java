package org.wickedsource.docxstamper.docx4j.processor;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.wickedsource.docxstamper.docx4j.walk.coordinates.ParagraphCoordinates;

public interface ICommentProcessor {

    public void commitChanges(XWPFDocument document);

    public void setCurrentParagraphCoordinates(ParagraphCoordinates coordinates);

}
