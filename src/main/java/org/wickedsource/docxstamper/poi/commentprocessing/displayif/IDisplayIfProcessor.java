package org.wickedsource.docxstamper.poi.commentprocessing.displayif;

public interface IDisplayIfProcessor {

    void displayParagraphIf(Boolean condition);

    void displayTableRowIf(Boolean condition);

    void displayTableIf(Boolean condition);

}
