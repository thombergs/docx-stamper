package org.wickedsource.docxstamper;

import org.docx4j.TextUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Tr;
import org.junit.jupiter.api.Test;
import org.wickedsource.docxstamper.util.DocumentUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConditionalDisplayOfTableRowsTest extends AbstractDocx4jTest {
	@Test
	public void test() throws Docx4JException, IOException {
		Name context = new Name("Homer");
		InputStream template = getClass().getResourceAsStream("ConditionalDisplayOfTableRowsTest.docx");
		WordprocessingMLPackage document = stampAndLoad(template, context);

		final List<Tbl> tablesFromObject = DocumentUtil.extractElements(document, Tbl.class);
		assertEquals(2, tablesFromObject.size());

		final List<Tr> parentTableRows = DocumentUtil.extractElements(tablesFromObject.get(0), Tr.class);
		// gets all the rows within the table and the nested table
		assertEquals(5, parentTableRows.size());

		final List<Tr> nestedTableRows = DocumentUtil.extractElements(tablesFromObject.get(1), Tr.class);
		assertEquals(2, nestedTableRows.size());

		final List<Tc> parentTableCells = DocumentUtil.extractElements(tablesFromObject.get(0), Tc.class);
		// gets all the cells within the table and the nested table
		assertEquals(5, parentTableCells.size());

		assertEquals("This row stays untouched.", getTextFromCell(parentTableCells.get(0)));
		assertEquals("This row stays untouched.", getTextFromCell(parentTableCells.get(1)));
		assertEquals("Also works on nested Tables", getTextFromCell(parentTableCells.get(3)));
		assertEquals("This row stays untouched.", getTextFromCell(parentTableCells.get(4)));
	}

	private String getTextFromCell(Tc tc) {
		List<P> paragraphsFromObject = DocumentUtil.extractElements(tc, P.class);
		assertEquals(1, paragraphsFromObject.size());
		return TextUtils.getText(paragraphsFromObject.get(0));
	}

	public record Name(String name) {
	}
}
