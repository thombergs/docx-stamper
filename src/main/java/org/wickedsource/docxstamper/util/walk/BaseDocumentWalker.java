package org.wickedsource.docxstamper.util.walk;

import org.docx4j.wml.*;

/**
 * This class is an abstract implementation of the {@link org.wickedsource.docxstamper.util.walk.DocumentWalker} interface.
 * It implements all methods of the interface and does nothing in the individual methods.
 * This makes it easier to implement a custom {@link org.wickedsource.docxstamper.util.walk.DocumentWalker} because the implementor
 * only has to implement the methods that are of interest.
 *
 * @author joseph
 * @version $Id: $Id
 */
public abstract class BaseDocumentWalker extends DocumentWalker {

    /**
     * Creates a new document walker that walks through the given document.
     *
     * @param contentAccessor the document to walk through.
     */
    protected BaseDocumentWalker(ContentAccessor contentAccessor) {
        super(contentAccessor);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onParagraph(P paragraph) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onRun(R run) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onTable(Tbl table) {

    }

    /** {@inheritDoc} */
    @Override
    protected void onTableCell(Tc tableCell) {

    }

    /** {@inheritDoc} */
    @Override
    protected void onTableRow(Tr tableRow) {

    }

    /** {@inheritDoc} */
    @Override
    protected void onCommentRangeStart(CommentRangeStart commentRangeStart) {

    }

    /** {@inheritDoc} */
    @Override
    protected void onCommentRangeEnd(CommentRangeEnd commentRangeEnd) {

    }

    /** {@inheritDoc} */
    @Override
    protected void onCommentReference(R.CommentReference commentReference) {
        
    }
}
