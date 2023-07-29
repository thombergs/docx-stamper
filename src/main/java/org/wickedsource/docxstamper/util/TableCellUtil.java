package org.wickedsource.docxstamper.util;

import jakarta.xml.bind.JAXBElement;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.wickedsource.docxstamper.api.DocxStamperException;

import java.util.function.Predicate;

/**
 * Utility class for table cells
 *
 * @author joseph
 * @version $Id: $Id
 */
public class TableCellUtil {

    private TableCellUtil() {
        throw new DocxStamperException("Utility class shouldn't be instantiated");
    }

	private static final ObjectFactory objectFactory = new ObjectFactory();

    /**
     * Checks if a table cell contains a paragraph or a table
     *
     * @param cell the table cell
     * @return true if the table cell contains a paragraph or a table, false otherwise
     */
	public static boolean hasNoParagraphOrTable(Tc cell) {
		Predicate<Object> isP = P.class::isInstance;
		Predicate<Object> isTbl = e -> e instanceof JAXBElement<?> jaxbElement && jaxbElement.getValue() instanceof Tbl;
		return cell.getContent()
				   .stream()
				   .noneMatch(isP.or(isTbl));
    }

    /**
     * Checks if a table cell contains a paragraph
     *
     * @param cell the table cell
	 */
	public static void addEmptyParagraph(Tc cell) {
		P paragraph = objectFactory.createP();
		paragraph.getContent().add(objectFactory.createR());
		cell.getContent().add(paragraph);
	}
}
