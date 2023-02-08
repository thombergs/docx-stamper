package org.wickedsource.docxstamper.util.walk;

import org.docx4j.wml.*;

public abstract class BaseDocumentWalker extends DocumentWalker {

    public BaseDocumentWalker(ContentAccessor contentAccessor) {
        super(contentAccessor);
    }

    @Override
    protected void onParagraph(P paragraph) {

    }

    @Override
    protected void onRun(R run) {

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

    @Override
    protected void onCommentRangeStart(CommentRangeStart commentRangeStart) {

    }

    @Override
    protected void onCommentRangeEnd(CommentRangeEnd commentRangeEnd) {

    }

    @Override
    protected void onCommentReference(R.CommentReference commentReference) {
        
    }
}
