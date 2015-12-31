package org.wickedsource.docxstamper.util;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.wickedsource.docxstamper.walk.coordinates.ParagraphCoordinates;
import org.wickedsource.docxstamper.walk.coordinates.TableCoordinates;
import org.wickedsource.docxstamper.walk.coordinates.TableRowCoordinates;

import java.util.HashMap;
import java.util.Map;

public class ObjectDeleter {

    private WordprocessingMLPackage document;

    private int objectsDeletedFromMainDocument = 0;

    private Map<ContentAccessor, Integer> deletedObjectsPerParent = new HashMap<>();

    public ObjectDeleter(WordprocessingMLPackage document) {
        this.document = document;
    }

    public void deleteParagraph(ParagraphCoordinates paragraphCoordinates) {
        if (paragraphCoordinates.getParentTableCellCoordinates() == null) {
            // global paragraph
            int indexToDelete = paragraphCoordinates.getIndex() - objectsDeletedFromMainDocument;
            document.getMainDocumentPart().getContent().remove(indexToDelete);
            objectsDeletedFromMainDocument++;
        } else {
            // paragraph within a table cell
            Tc parentCell = paragraphCoordinates.getParentTableCellCoordinates().getCell();
            deleteFromCell(parentCell, paragraphCoordinates.getIndex());
        }
    }

    private void deleteFromCell(Tc cell, int index) {
        Integer objectsDeletedFromParent = deletedObjectsPerParent.get(cell);
        if (objectsDeletedFromParent == null) {
            objectsDeletedFromParent = 0;
        }
        index -= objectsDeletedFromParent;
        cell.getContent().remove(index);
        if (!TableCellUtil.hasAtLeastOneParagraphOrTable(cell)) {
            TableCellUtil.addEmptyParagraph(cell);
        }
        deletedObjectsPerParent.put(cell, objectsDeletedFromParent + 1);
        // TODO: find out why border lines are removed in some cells after having deleted a paragraph
    }

    public void deleteTable(TableCoordinates tableCoordinates) {
        if (tableCoordinates.getParentTableCellCoordinates() == null) {
            // global table
            int indexToDelete = tableCoordinates.getIndex() - objectsDeletedFromMainDocument;
            document.getMainDocumentPart().getContent().remove(indexToDelete);
            objectsDeletedFromMainDocument++;
        } else {
            // nested table within an table cell
            Tc parentCell = tableCoordinates.getParentTableCellCoordinates().getCell();
            deleteFromCell(parentCell, tableCoordinates.getIndex());
        }
    }

    public void deleteTableRow(TableRowCoordinates tableRowCoordinates) {
        Tbl table = tableRowCoordinates.getParentTableCoordinates().getTable();
        int index = tableRowCoordinates.getIndex();
        Integer objectsDeletedFromTable = deletedObjectsPerParent.get(table);
        if (objectsDeletedFromTable == null) {
            objectsDeletedFromTable = 0;
        }
        index -= objectsDeletedFromTable;
        table.getContent().remove(index);
        deletedObjectsPerParent.put(table, objectsDeletedFromTable + 1);
    }

}
