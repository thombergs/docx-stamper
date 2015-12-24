package org.wickedsource.docxstamper.commentprocessing.displayif;

public interface IDisplayIfProcessor {

    void displayParagraphIf(Boolean condition);

    void displayTableRowIf(Boolean condition);

    void displayTableIf(Boolean condition);

}
