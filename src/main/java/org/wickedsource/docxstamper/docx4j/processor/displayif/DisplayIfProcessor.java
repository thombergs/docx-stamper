package org.wickedsource.docxstamper.docx4j.processor.displayif;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.wickedsource.docxstamper.docx4j.processor.BaseCommentProcessor;
import org.wickedsource.docxstamper.docx4j.processor.CommentProcessingException;
import org.wickedsource.docxstamper.docx4j.walk.coordinates.ParagraphCoordinates;
import org.wickedsource.docxstamper.docx4j.walk.coordinates.TableCoordinates;
import org.wickedsource.docxstamper.docx4j.walk.coordinates.TableRowCoordinates;

import java.util.ArrayList;
import java.util.List;

public class DisplayIfProcessor extends BaseCommentProcessor implements IDisplayIfProcessor {

    private List<ParagraphCoordinates> paragraphsToBeRemoved = new ArrayList<>();

    private List<TableCoordinates> tablesToBeRemoved = new ArrayList<>();

    private List<TableRowCoordinates> tableRowsToBeRemoved = new ArrayList<>();

    private int indexCorrection = 0;

    @Override
    public void commitChanges(XWPFDocument document) {
        removeParagraphs(document);
        removeTables(document);
        removeTableRows(document);
    }

    private void removeParagraphs(XWPFDocument document) {
        for (ParagraphCoordinates pCoords : paragraphsToBeRemoved) {
            if (pCoords.getParentTableCellCoordinates() == null) {
                // paragraph is on main level (i.e. not in a table)
                document.removeBodyElement(pCoords.getIndex() - indexCorrection);
                indexCorrection++;
            } else {
                // paragraph is within a table cell
                //  XWPFTableCell cell = pCoords.getParentTableCellCoordinates().getCell();
                //  cell.removeParagraph(pCoords.getIndex());
                // TODO: POI seems to remove the whole table instead of just the paragraph within...what to do?
                // TODO: cell-local index correction necessary if more than one table or paragraph is removed from the same cell?
                throw new CommentProcessingException("Removing a paragraph from within a table is currently not supported!", pCoords);
            }
        }
    }


    private void removeTables(XWPFDocument document) {
        for (TableCoordinates tCoords : tablesToBeRemoved) {
            if (tCoords.getParentTableCellCoordinates() == null) {
                // table is on main level (i.e. not nested in another table)
                document.removeBodyElement(tCoords.getIndex() - indexCorrection);
                indexCorrection++;
            } else {
                // table is nested within a table cell
//                XWPFTableCell cell = tCoords.getParentTableCellCoordinates().getCell();
                // TODO: XWPFTableCell does not provide a method for removing a table from within a table cell. Any workaround possible?
                // TODO: cell-local index correction necessary if more than one table or paragraph is removed from the same cell?
                throw new CommentProcessingException("Removing a table from within another table is currently not supported!", tCoords);
            }
        }
    }

    private void removeTableRows(XWPFDocument document) {
        int rowIndexCorrection = 0;
        for (TableRowCoordinates rCoords : tableRowsToBeRemoved) {
//            rCoords.getParentTableCoordinates().getTable().removeRow(rCoords.getIndex() - rowIndexCorrection);
//            rowIndexCorrection++;
        }
    }

    @Override
    public void displayParagraphIf(Boolean condition) {
        if (!condition) {
            ParagraphCoordinates coords = getCurrentParagraphCoordinates();
            paragraphsToBeRemoved.add(coords);
        }
    }

    @Override
    public void displayTableIf(Boolean condition) {
        if (!condition) {
            ParagraphCoordinates pCoords = getCurrentParagraphCoordinates();
            if (pCoords.getParentTableCellCoordinates() == null ||
                    pCoords.getParentTableCellCoordinates().getParentTableRowCoordinates() == null ||
                    pCoords.getParentTableCellCoordinates().getParentTableRowCoordinates().getParentTableCoordinates() == null) {
                throw new CommentProcessingException("Paragraph is not within a table!", pCoords);
            }
            tablesToBeRemoved.add(pCoords.getParentTableCellCoordinates().getParentTableRowCoordinates().getParentTableCoordinates());
        }
    }

    @Override
    public void displayTableRowIf(Boolean condition) {
        if (!condition) {
            ParagraphCoordinates pCoords = getCurrentParagraphCoordinates();
            if (pCoords.getParentTableCellCoordinates() == null ||
                    pCoords.getParentTableCellCoordinates().getParentTableRowCoordinates() == null) {
                throw new CommentProcessingException("Paragraph is not within a table!", pCoords);
            }
            tableRowsToBeRemoved.add(pCoords.getParentTableCellCoordinates().getParentTableRowCoordinates());
        }
    }
}
