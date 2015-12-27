package org.wickedsource.docxstamper.docx4j.walk;

import org.docx4j.wml.*;

public abstract class BaseDocumentWalker extends DocumentWalker {

    public BaseDocumentWalker(ContentAccessor contentAccessor) {
        super(contentAccessor);
    }

    @Override
    protected void onParagraph(P paragraph) {

    }

    @Override
    protected void onTable(Tbl table) {

    }

    @Override
    protected void onTableCell(Tc tableCell) {

    }

    @Override
    protected void onTableRow(Tr tableRow) {

    }
}
