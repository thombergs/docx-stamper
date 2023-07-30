package io.reflectoring.docxstamper.processor.displayif;

import io.reflectoring.docxstamper.api.coordinates.ParagraphCoordinates;
import io.reflectoring.docxstamper.api.coordinates.TableCoordinates;
import io.reflectoring.docxstamper.api.coordinates.TableRowCoordinates;
import io.reflectoring.docxstamper.util.ObjectDeleter;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import io.reflectoring.docxstamper.processor.BaseCommentProcessor;
import io.reflectoring.docxstamper.processor.CommentProcessingException;

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

	@Override
	public void reset() {
		paragraphsToBeRemoved = new ArrayList<>();
		tablesToBeRemoved = new ArrayList<>();
		tableRowsToBeRemoved = new ArrayList<>();
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
