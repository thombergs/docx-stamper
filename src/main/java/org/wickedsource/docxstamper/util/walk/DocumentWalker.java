package org.wickedsource.docxstamper.util.walk;

import org.docx4j.XmlUtils;
import org.docx4j.wml.*;

import javax.xml.bind.JAXBElement;

public abstract class DocumentWalker {

    private ContentAccessor contentAccessor;

    public DocumentWalker(ContentAccessor contentAccessor) {
        this.contentAccessor = contentAccessor;
    }

    public void walk() {
        for (Object contentElement : contentAccessor.getContent()) {
            Object unwrappedObject = XmlUtils.unwrap(contentElement);
            if (unwrappedObject instanceof P) {
                P p = (P) unwrappedObject;
                walkParagraph(p);
            } else if (unwrappedObject instanceof Tbl) {
                Tbl table = (Tbl) unwrappedObject;
                walkTable(table);
            } else if (unwrappedObject instanceof Tr) {
                Tr row = (Tr) unwrappedObject;
                walkTableRow(row);
            } else if (unwrappedObject instanceof Tc) {
                Tc cell = (Tc) unwrappedObject;
                walkTableCell(cell);
            } else if (unwrappedObject instanceof CommentRangeStart) {
                CommentRangeStart commentRangeStart = (CommentRangeStart) unwrappedObject;
                onCommentRangeStart(commentRangeStart);
            } else if (unwrappedObject instanceof CommentRangeEnd) {
                CommentRangeEnd commentRangeEnd = (CommentRangeEnd) unwrappedObject;
                onCommentRangeEnd(commentRangeEnd);
            }
        }
    }

    private void walkTable(Tbl table) {
        onTable(table);
        for (Object contentElement : table.getContent()) {
            Object unwrappedObject = XmlUtils.unwrap(contentElement);
            if (unwrappedObject instanceof Tr) {
                Tr row = (Tr) unwrappedObject;
                walkTableRow(row);
            }
        }
    }


    private void walkTableRow(Tr row) {
        onTableRow(row);
        for (Object rowContentElement : row.getContent()) {
            Object unwrappedObject = XmlUtils.unwrap(rowContentElement);
            if (unwrappedObject instanceof Tc) {
                Tc cell = (Tc) unwrappedObject;
                walkTableCell(cell);
            }
        }
    }

    private void walkTableCell(Tc cell) {
        onTableCell(cell);
        for (Object cellContentElement : cell.getContent()) {
            Object unwrappedObject = XmlUtils.unwrap(cellContentElement);
            if (unwrappedObject instanceof P) {
                P p = (P) cellContentElement;
                walkParagraph(p);
            } else if (unwrappedObject instanceof Tbl) {
                Tbl nestedTable = (Tbl) unwrappedObject;
                walkTable(nestedTable);
            } else if (unwrappedObject instanceof CommentRangeStart) {
                CommentRangeStart commentRangeStart = (CommentRangeStart) unwrappedObject;
                onCommentRangeStart(commentRangeStart);
            } else if (unwrappedObject instanceof CommentRangeEnd) {
                CommentRangeEnd commentRangeEnd = (CommentRangeEnd) unwrappedObject;
                onCommentRangeEnd(commentRangeEnd);
            }
        }
    }

    private void walkParagraph(P p) {
        onParagraph(p);
        for (Object element : p.getContent()) {
            Object unwrappedObject = XmlUtils.unwrap(element);
            if (unwrappedObject instanceof CommentRangeStart) {
                CommentRangeStart commentRangeStart = (CommentRangeStart) unwrappedObject;
                onCommentRangeStart(commentRangeStart);
            } else if (unwrappedObject instanceof CommentRangeEnd) {
                CommentRangeEnd commentRangeEnd = (CommentRangeEnd) unwrappedObject;
                onCommentRangeEnd(commentRangeEnd);
            }
        }
    }

    protected abstract void onParagraph(P paragraph);

    protected abstract void onTable(Tbl table);

    protected abstract void onTableCell(Tc tableCell);

    protected abstract void onTableRow(Tr tableRow);

    protected abstract void onCommentRangeStart(CommentRangeStart commentRangeStart);

    protected abstract void onCommentRangeEnd(CommentRangeEnd commentRangeEnd);

}
