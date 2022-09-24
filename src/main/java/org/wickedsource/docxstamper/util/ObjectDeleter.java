package org.wickedsource.docxstamper.util;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.wickedsource.docxstamper.api.coordinates.ParagraphCoordinates;
import org.wickedsource.docxstamper.api.coordinates.TableCellCoordinates;
import org.wickedsource.docxstamper.api.coordinates.TableCoordinates;
import org.wickedsource.docxstamper.api.coordinates.TableRowCoordinates;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ObjectDeleter {

    private final WordprocessingMLPackage document;

    private final List<Integer> deletedObjectsIndexes = new ArrayList<>(10);

    private final Map<ContentAccessor, Integer> deletedObjectsPerParent = new HashMap<>();

    public ObjectDeleter(WordprocessingMLPackage document) {
        this.document = document;
    }

    public void deleteParagraph(ParagraphCoordinates paragraphCoordinates) {
        deleteTableOrParagraph(paragraphCoordinates.getIndex(), paragraphCoordinates.getParentTableCellCoordinates());
    }

    public void deleteTable(TableCoordinates tableCoordinates) {
        deleteTableOrParagraph(tableCoordinates.getIndex(), tableCoordinates.getParentTableCellCoordinates());
    }

    private void deleteTableOrParagraph(int index, TableCellCoordinates parentTableCellCoordinates) {
        if (parentTableCellCoordinates == null) {
            // global paragraph
            int indexToDelete = getOffset(index);
            document.getMainDocumentPart().getContent().remove(indexToDelete);
            deletedObjectsIndexes.add(index);
        } else {
            // paragraph within a table cell
            Tc parentCell = parentTableCellCoordinates.getCell();
            deleteFromCell(parentCell, index);
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
        // TODO: find out why border lines are removed in some cells after having
        // deleted a paragraph
    }

    /**
     * Get new index of element to be deleted, taking into account previously
     * deleted elements
     *
     * @param initialIndex initial index of the element to be deleted
     * @return the index of the item to be removed
     */
    private int getOffset(final int initialIndex) {
        int newIndex = initialIndex;
        for (Integer deletedIndex : this.deletedObjectsIndexes) {
            if (initialIndex > deletedIndex) {
                newIndex--;
            }
        }
        return newIndex;
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
