package org.wickedsource.docxstamper.util;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.wickedsource.docxstamper.api.coordinates.ParagraphCoordinates;
import org.wickedsource.docxstamper.api.coordinates.TableCoordinates;
import org.wickedsource.docxstamper.api.coordinates.TableRowCoordinates;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ObjectDeleter {

	private WordprocessingMLPackage document;

	private List<Integer> deletedObjectsIndexes = new ArrayList<>(10);

	private Map<ContentAccessor, Integer> deletedObjectsPerParent = new HashMap<>();

	public ObjectDeleter(WordprocessingMLPackage document) {
		this.document = document;
	}

	public void deleteParagraph(ParagraphCoordinates paragraphCoordinates) {
		if (paragraphCoordinates.getParentTableCellCoordinates() == null) {
			// global paragraph
			int indexToDelete = getOffset(paragraphCoordinates.getIndex());
			document.getMainDocumentPart().getContent().remove(indexToDelete);
			deletedObjectsIndexes.add(paragraphCoordinates.getIndex());
		} else {
			// paragraph within a table cell
			Tc parentCell = paragraphCoordinates.getParentTableCellCoordinates().getCell();
			deleteFromCell(parentCell, paragraphCoordinates.getIndex());
		}
	}

	/**
	 * Get new index of element to be deleted, taking into account previously
	 * deleted elements
	 * 
	 * @param initialIndex
	 *            initial index of the element to be deleted
	 * @param deletedObjects
	 *            list of indexes of already deleted elements
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

	public void deleteTable(TableCoordinates tableCoordinates) {
		if (tableCoordinates.getParentTableCellCoordinates() == null) {
			// global table
			int indexToDelete = getOffset(tableCoordinates.getIndex());
			document.getMainDocumentPart().getContent().remove(indexToDelete);
			deletedObjectsIndexes.add(tableCoordinates.getIndex());
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
