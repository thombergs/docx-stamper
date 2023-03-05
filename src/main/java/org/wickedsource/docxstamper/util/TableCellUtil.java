package org.wickedsource.docxstamper.util;

import jakarta.xml.bind.JAXBElement;
import lombok.experimental.UtilityClass;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;

import java.util.function.Predicate;

@UtilityClass
public class TableCellUtil {
	private static final ObjectFactory objectFactory = new ObjectFactory();

	public static boolean hasNoParagraphOrTable(Tc cell) {
		Predicate<Object> isP = e -> e instanceof P;
		Predicate<Object> isTbl = e -> e instanceof JAXBElement<?> jaxbElement && jaxbElement.getValue() instanceof Tbl;
		return cell.getContent()
				   .stream()
				   .noneMatch(isP.or(isTbl));
	}

	public static void addEmptyParagraph(Tc cell) {
		P paragraph = objectFactory.createP();
		paragraph.getContent().add(objectFactory.createR());
		cell.getContent().add(paragraph);
	}
}
