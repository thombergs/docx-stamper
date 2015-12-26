package org.wickedsource.docxstamper.docx4j.walk;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Tr;
import org.wickedsource.docxstamper.docx4j.walk.coordinates.ParagraphCoordinates;
import org.wickedsource.docxstamper.docx4j.walk.coordinates.TableCellCoordinates;
import org.wickedsource.docxstamper.docx4j.walk.coordinates.TableCoordinates;
import org.wickedsource.docxstamper.docx4j.walk.coordinates.TableRowCoordinates;

import javax.xml.bind.JAXBElement;

public abstract class DocumentWalker {

    private WordprocessingMLPackage document;

    public DocumentWalker(WordprocessingMLPackage document) {
        this.document = document;
    }

    public void walk() {
        int elementIndex = 0;
        for (Object contentElement : document.getMainDocumentPart().getContent()) {
            if (contentElement instanceof P) {
                P p = (P) contentElement;
                ParagraphCoordinates coordinates = new ParagraphCoordinates(p, elementIndex);
                onParagraph(coordinates);
            } else if (contentElement instanceof JAXBElement && ((JAXBElement) contentElement).getValue() instanceof Tbl) {
                Tbl table = (Tbl) ((JAXBElement) contentElement).getValue();
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
            if (contentElement instanceof Tr) {
                Tr row = (Tr) contentElement;
                TableRowCoordinates rowCoordinates = new TableRowCoordinates(row, rowIndex, tableCoordinates);
                onTableRow(rowCoordinates);
                int cellIndex = 0;
                for (Object rowContentElement : row.getContent()) {
                    if (rowContentElement instanceof JAXBElement && ((JAXBElement) rowContentElement).getValue() instanceof Tc) {
                        Tc cell = (Tc) ((JAXBElement) rowContentElement).getValue();
                        TableCellCoordinates cellCoordinates = new TableCellCoordinates(cell, cellIndex, rowCoordinates);
                        onTableCell(cellCoordinates);
                        int elementIndex = 0;
                        for (Object cellContentElement : cell.getContent()) {
                            if (cellContentElement instanceof P) {
                                P p = (P) cellContentElement;
                                ParagraphCoordinates paragraphCoordinates = new ParagraphCoordinates(p, elementIndex, cellCoordinates);
                                onParagraph(paragraphCoordinates);
                            } else if (cellContentElement instanceof JAXBElement && ((JAXBElement) cellContentElement).getValue() instanceof Tbl) {
                                Tbl nestedTable = (Tbl) ((JAXBElement) cellContentElement).getValue();
                                TableCoordinates innerTableCoordinates = new TableCoordinates(nestedTable, elementIndex, cellCoordinates);
                                walkTable(innerTableCoordinates);
                            }
                            elementIndex++;
                        }
                    }
                    cellIndex++;
                }
                rowIndex++;
            }
        }
    }

    protected abstract void onParagraph(ParagraphCoordinates paragraphCoordinates);

    protected abstract void onTable(TableCoordinates tableCoordinates);

    protected abstract void onTableCell(TableCellCoordinates tableCellCoordinates);

    protected abstract void onTableRow(TableRowCoordinates tableRowCoordinates);

}
