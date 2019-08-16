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
            if (XmlUtils.unwrap(contentElement) instanceof Tr) {
                Tr row = (Tr) contentElement;
                walkTableRow(row);
            }
        }
    }


    private void walkTableRow(Tr row) {
        onTableRow(row);
        for (Object rowContentElement : row.getContent()) {
            if (XmlUtils.unwrap(rowContentElement) instanceof Tc) {
                Tc cell = rowContentElement instanceof Tc ? (Tc) rowContentElement : (Tc) ((JAXBElement) rowContentElement).getValue();
                walkTableCell(cell);
            }
        }
    }

    private void walkTableCell(Tc cell) {
        onTableCell(cell);
        for (Object cellContentElement : cell.getContent()) {
            if (XmlUtils.unwrap(cellContentElement) instanceof P) {
                P p = (P) cellContentElement;
                walkParagraph(p);
            } else if (XmlUtils.unwrap(cellContentElement) instanceof Tbl) {
                Tbl nestedTable = (Tbl) ((JAXBElement) cellContentElement).getValue();
                walkTable(nestedTable);
            }
        }
    }

    private void walkParagraph(P p) {
        onParagraph(p);
        for (Object element : p.getContent()) {
            if (XmlUtils.unwrap(element) instanceof CommentRangeStart) {
                CommentRangeStart commentRangeStart = (CommentRangeStart) element;
                onCommentRangeStart(commentRangeStart);
            } else if (XmlUtils.unwrap(element) instanceof CommentRangeEnd) {
                CommentRangeEnd commentRangeEnd = (CommentRangeEnd) element;
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
