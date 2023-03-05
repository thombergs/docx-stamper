package org.wickedsource.docxstamper.util;

import jakarta.xml.bind.JAXBElement;
import org.docx4j.XmlUtils;
import org.docx4j.wml.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

public class ObjectDeleter {
	private final Logger logger = LoggerFactory.getLogger(ObjectDeleter.class);

	public void deleteObject(Object object) {
		if (object instanceof P) {
			deleteParagraph((P) object);
		} else {
			Object unwrappedObject = XmlUtils.unwrap(object);
			if (unwrappedObject instanceof Tbl) {
				deleteTable((Tbl) unwrappedObject);
			}
		}
	}

	public void deleteParagraph(P paragraph) {
		if (paragraph.getParent() instanceof Tc parentCell) {
			// paragraph within a table cell
            deleteFromCell(parentCell, paragraph);
		} else {
			((ContentAccessor) paragraph.getParent()).getContent().remove(paragraph);
		}
	}

	public void deleteTable(Tbl table) {
		if (table.getParent() instanceof Tc parentCell) {
			// nested table within a table cell
            deleteFromCell(parentCell, table);
		} else {
			// global table
			((ContentAccessor) table.getParent()).getContent().remove(table.getParent());
			// iterate through the containing list to find the jaxb element that contains the table.
			for (Iterator<Object> iterator = ((ContentAccessor) table.getParent()).getContent()
																				  .listIterator(); iterator.hasNext(); ) {
				Object next = iterator.next();
				if (next instanceof JAXBElement && ((JAXBElement) next).getValue().equals(table)) {
					iterator.remove();
					break;
				}
			}
		}
	}

	private void deleteFromCell(Tc cell, P paragraph) {
		cell.getContent().remove(paragraph);
		if (TableCellUtil.hasNoParagraphOrTable(cell)) {
			TableCellUtil.addEmptyParagraph(cell);
		}
		// TODO: find out why border lines are removed in some cells after having deleted a paragraph
	}

	private void deleteFromCell(Tc cell, Object obj) {
		if (!(obj instanceof Tbl || obj instanceof P)) {
			throw new AssertionError("Only delete Tables or Paragraphs with this method.");
		}
		cell.getContent().remove(obj);
		if (TableCellUtil.hasNoParagraphOrTable(cell)) {
			TableCellUtil.addEmptyParagraph(cell);
		}
		// TODO: find out why border lines are removed in some cells after having deleted a paragraph
	}

	public void deleteTableRow(Tr tableRow) {
		if (tableRow.getParent() instanceof Tbl table) {
            table.getContent().remove(tableRow);
		} else {
			logger.error("Table row is not contained within a table. Unable to remove");
		}
	}

}
