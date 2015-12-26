package org.wickedsource.docxstamper.docx4j.processor.displayif;

public interface IDisplayIfProcessor {

    void displayParagraphIf(Boolean condition);

    void displayTableRowIf(Boolean condition);

    void displayTableIf(Boolean condition);

}
