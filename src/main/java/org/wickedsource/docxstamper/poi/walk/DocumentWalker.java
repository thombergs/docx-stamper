package org.wickedsource.docxstamper.poi.walk;

import org.apache.poi.xwpf.usermodel.*;
import org.wickedsource.docxstamper.poi.coordinates.ParagraphCoordinates;
import org.wickedsource.docxstamper.poi.coordinates.TableCellCoordinates;
import org.wickedsource.docxstamper.poi.coordinates.TableCoordinates;
import org.wickedsource.docxstamper.poi.coordinates.TableRowCoordinates;

public abstract class DocumentWalker {

    private XWPFDocument document;

    public DocumentWalker(XWPFDocument document) {
        this.document = document;
    }

    public void walk() {
        int elementIndex = 0;
        for (IBodyElement bodyElement : document.getBodyElements()) {
            if (bodyElement instanceof XWPFParagraph) {
                XWPFParagraph p = (XWPFParagraph) bodyElement;
                ParagraphCoordinates coordinates = new ParagraphCoordinates(p, elementIndex, null);
                onParagraph(coordinates);
            } else if (bodyElement instanceof XWPFTable) {
                XWPFTable table = (XWPFTable) bodyElement;
                TableCoordinates coordinates = new TableCoordinates(table, elementIndex, null);
                walkTable(coordinates);
            }
            elementIndex++;
        }
    }

    private void walkTable(TableCoordinates tableCoordinates) {
        onTable(tableCoordinates);
        int rowIndex = 0;
        for (XWPFTableRow row : tableCoordinates.getTable().getRows()) {
            TableRowCoordinates tableRowCoordinates = new TableRowCoordinates(row, rowIndex, tableCoordinates);
            onTableRow(tableRowCoordinates);
            int cellIndex = 0;
            for (XWPFTableCell cell : row.getTableCells()) {
                TableCellCoordinates tableCellCoordinates = new TableCellCoordinates(cell, cellIndex, tableRowCoordinates);
                onTableCell(tableCellCoordinates);
                int elementIndex = 0;
                for (IBodyElement bodyElement : cell.getBodyElements()) {
                    if (bodyElement instanceof XWPFParagraph) {
                        XWPFParagraph p = (XWPFParagraph) bodyElement;
                        ParagraphCoordinates paragraphCoordinates = new ParagraphCoordinates(p, elementIndex, tableCellCoordinates);
                        onParagraph(paragraphCoordinates);
                    } else if (bodyElement instanceof XWPFTable) {
                        XWPFTable nestedTable = (XWPFTable) bodyElement;
                        TableCoordinates innerTableCoordinates = new TableCoordinates(nestedTable, elementIndex, tableCellCoordinates);
                        walkTable(innerTableCoordinates);
                    }
                    elementIndex++;
                }
                cellIndex++;
            }
            rowIndex++;
        }
    }

    protected abstract void onParagraph(ParagraphCoordinates paragraphCoordinates);

    protected abstract void onTable(TableCoordinates tableCoordinates);

    protected abstract void onTableRow(TableRowCoordinates tableRowCoordinates);

    protected abstract void onTableCell(TableCellCoordinates tableCellCoordinates);

}
