package org.wickedsource.docxstamper.walk.coordinates;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Tr;

import javax.xml.bind.JAXBElement;

public abstract class CoordinatesWalker {

    private WordprocessingMLPackage document;

    public CoordinatesWalker(WordprocessingMLPackage document) {
        this.document = document;
    }

    public void walk() {
        int elementIndex = 0;
        for (Object contentElement : document.getMainDocumentPart().getContent()) {
            Object unwrappedObject = XmlUtils.unwrap(contentElement);
            if (unwrappedObject instanceof P) {
                P p = (P) unwrappedObject;
                ParagraphCoordinates coordinates = new ParagraphCoordinates(p, elementIndex);
                onParagraph(coordinates);
            } else if (unwrappedObject instanceof Tbl) {
                Tbl table = (Tbl) unwrappedObject;
                TableCoordinates tableCoordinates = new TableCoordinates(table, elementIndex);
                walkTable(tableCoordinates);
            }
            elementIndex++;
        }
    }

    private void walkTable(TableCoordinates tableCoordinates) {
        onTable(tableCoordinates);
        int rowIndex = 0;
        for (Object contentElement : tableCoordinates.getTable().getContent()) {
            if (XmlUtils.unwrap(contentElement) instanceof Tr) {
                Tr row = (Tr) contentElement;
                TableRowCoordinates rowCoordinates = new TableRowCoordinates(row, rowIndex, tableCoordinates);
                walkTableRow(rowCoordinates);
            }
            rowIndex++;
        }
    }


    private void walkTableRow(TableRowCoordinates rowCoordinates) {
        onTableRow(rowCoordinates);
        int cellIndex = 0;
        for (Object rowContentElement : rowCoordinates.getRow().getContent()) {
            if (XmlUtils.unwrap(rowContentElement) instanceof Tc) {
                Tc cell = (Tc) ((JAXBElement) rowContentElement).getValue();
                TableCellCoordinates cellCoordinates = new TableCellCoordinates(cell, cellIndex, rowCoordinates);
                walkTableCell(cellCoordinates);
            }
            cellIndex++;
        }
    }

    private void walkTableCell(TableCellCoordinates cellCoordinates) {
        onTableCell(cellCoordinates);
        int elementIndex = 0;
        for (Object cellContentElement : cellCoordinates.getCell().getContent()) {
            if (XmlUtils.unwrap(cellContentElement) instanceof P) {
                P p = (P) cellContentElement;
                ParagraphCoordinates paragraphCoordinates = new ParagraphCoordinates(p, elementIndex, cellCoordinates);
                onParagraph(paragraphCoordinates);
            } else if (XmlUtils.unwrap(cellContentElement) instanceof Tbl) {
                Tbl nestedTable = (Tbl) ((JAXBElement) cellContentElement).getValue();
                TableCoordinates innerTableCoordinates = new TableCoordinates(nestedTable, elementIndex, cellCoordinates);
                walkTable(innerTableCoordinates);
            }
            elementIndex++;
        }
    }

    protected abstract void onParagraph(ParagraphCoordinates paragraphCoordinates);

    protected abstract void onTable(TableCoordinates tableCoordinates);

    protected abstract void onTableCell(TableCellCoordinates tableCellCoordinates);

    protected abstract void onTableRow(TableRowCoordinates tableRowCoordinates);

}
