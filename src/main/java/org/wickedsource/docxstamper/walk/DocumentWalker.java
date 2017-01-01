package org.wickedsource.docxstamper.walk;

import org.docx4j.XmlUtils;
import org.docx4j.wml.*;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

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
                onParagraph(p);
            } else if (unwrappedObject instanceof Tbl) {
                Tbl table = (Tbl) unwrappedObject;
                walkTable(table);
            } else if (unwrappedObject instanceof Tr) {
                Tr row = (Tr) unwrappedObject;
                walkTableRow(row);
            } else if (unwrappedObject instanceof Tc) {
                Tc cell = (Tc) unwrappedObject;
                walkTableCell(cell);
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
                Tc cell = rowContentElement instanceof Tc? rowContentElement: (Tc) ((JAXBElement) rowContentElement).getValue();
                walkTableCell(cell);
            }
        }
    }

    private void walkTableCell(Tc cell) {
        onTableCell(cell);
        for (Object cellContentElement : cell.getContent()) {
            if (XmlUtils.unwrap(cellContentElement) instanceof P) {
                P p = (P) cellContentElement;
                onParagraph(p);
            } else if (XmlUtils.unwrap(cellContentElement) instanceof Tbl) {
                Tbl nestedTable = (Tbl) ((JAXBElement) cellContentElement).getValue();
                walkTable(nestedTable);
            }
        }
    }

    protected abstract void onParagraph(P paragraph);

    protected abstract void onTable(Tbl table);

    protected abstract void onTableCell(Tc tableCell);

    protected abstract void onTableRow(Tr tableRow);

}
