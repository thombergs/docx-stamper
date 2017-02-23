package org.wickedsource.docxstamper.processor.displayif;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.wickedsource.docxstamper.processor.BaseCommentProcessor;
import org.wickedsource.docxstamper.processor.CommentProcessingException;
import org.wickedsource.docxstamper.util.ObjectDeleter;
import org.wickedsource.docxstamper.walk.coordinates.ParagraphCoordinates;
import org.wickedsource.docxstamper.walk.coordinates.TableCoordinates;
import org.wickedsource.docxstamper.walk.coordinates.TableRowCoordinates;

import java.util.ArrayList;
import java.util.List;

public class DisplayIfProcessor extends BaseCommentProcessor implements IDisplayIfProcessor {

    private List<ParagraphCoordinates> paragraphsToBeRemoved = new ArrayList<>();

    private List<TableCoordinates> tablesToBeRemoved = new ArrayList<>();

    private List<TableRowCoordinates> tableRowsToBeRemoved = new ArrayList<>();

    @Override
    public void commitChanges(WordprocessingMLPackage document) {
        ObjectDeleter deleter = new ObjectDeleter(document);
        removeParagraphs(deleter);
        removeTables(deleter);
        removeTableRows(deleter);
    }

    private void removeParagraphs(ObjectDeleter deleter) {
        for (ParagraphCoordinates pCoords : paragraphsToBeRemoved) {
            deleter.deleteParagraph(pCoords);
        }
    }

    private void removeTables(ObjectDeleter deleter) {
        for (TableCoordinates tCoords : tablesToBeRemoved) {
            deleter.deleteTable(tCoords);
        }
    }

    private void removeTableRows(ObjectDeleter deleter) {
        for (TableRowCoordinates rCoords : tableRowsToBeRemoved) {
            deleter.deleteTableRow(rCoords);
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
