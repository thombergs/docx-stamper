package org.wickedsource.docxstamper.util;

import jakarta.xml.bind.JAXBElement;
import org.docx4j.wml.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wickedsource.docxstamper.api.DocxStamperException;

import java.util.Iterator;

/**
 * Utility class for deleting objects from a {@link org.docx4j.wml.Document}.
 *
 * @author joseph
 * @version $Id: $Id
 */
public class ObjectDeleter {
	private static final Logger log = LoggerFactory.getLogger(ObjectDeleter.class);

    private ObjectDeleter() {
        throw new DocxStamperException("Utility class shouldn't be instantiated");
	}

    /**
     * Deletes the given paragraph from the document.
     *
     * @param paragraph the paragraph to delete.
     */
    public static void deleteParagraph(P paragraph) {
		if (paragraph.getParent() instanceof Tc parentCell) {
			// paragraph within a table cell
            ObjectDeleter.deleteFromCell(parentCell, paragraph);
		} else {
			((ContentAccessor) paragraph.getParent()).getContent().remove(paragraph);
        }
    }

    /**
     * Deletes the given table from the document.
     *
     * @param table the table to delete.
     */
    public static void deleteTable(Tbl table) {
		if (table.getParent() instanceof Tc parentCell) {
			// nested table within a table cell
            ObjectDeleter.deleteFromCell(parentCell, table);
		} else {
			// global table
			((ContentAccessor) table.getParent()).getContent().remove(table.getParent());
			// iterate through the containing list to find the jaxb element that contains the table.
			for (Iterator<Object> iterator = ((ContentAccessor) table.getParent()).getContent()
																				  .listIterator(); iterator.hasNext(); ) {
				Object next = iterator.next();
				if (next instanceof JAXBElement element && element.getValue().equals(table)) {
					iterator.remove();
					break;
                }
            }
        }
    }

    private static void deleteFromCell(Tc cell, P paragraph) {
		cell.getContent().remove(paragraph);
		if (TableCellUtil.hasNoParagraphOrTable(cell)) {
			TableCellUtil.addEmptyParagraph(cell);
		}
		// TODO: find out why border lines are removed in some cells after having deleted a paragraph
    }

    private static void deleteFromCell(Tc cell, Object obj) {
		if (!(obj instanceof Tbl || obj instanceof P)) {
			throw new AssertionError("Only delete Tables or Paragraphs with this method.");
		}
		cell.getContent().remove(obj);
		if (TableCellUtil.hasNoParagraphOrTable(cell)) {
			TableCellUtil.addEmptyParagraph(cell);
		}
		// TODO: find out why border lines are removed in some cells after having deleted a paragraph
    }

    /**
     * Deletes the given table row from the document.
     *
     * @param tableRow the table row to delete.
     */
    public static void deleteTableRow(Tr tableRow) {
		if (tableRow.getParent() instanceof Tbl table) {
            table.getContent().remove(tableRow);
        } else {
            log.error("Table row is not contained within a table. Unable to remove");
		}
	}

}
