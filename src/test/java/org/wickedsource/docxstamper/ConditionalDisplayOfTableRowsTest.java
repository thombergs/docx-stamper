package org.wickedsource.docxstamper;

import org.docx4j.TextUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.wml.P;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Tr;
import org.junit.jupiter.api.Test;
import org.wickedsource.docxstamper.util.DocumentUtil;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConditionalDisplayOfTableRowsTest {
	@Test
	public void test() throws Docx4JException, IOException {
		var context = new Name("Homer");
		var template = getClass().getResourceAsStream("ConditionalDisplayOfTableRowsTest.docx");
		var stamper = new TestDocxStamper<Name>();
		var document = stamper.stampAndLoad(template, context);
		
		var tablesFromObject = DocumentUtil.streamElements(document, Tbl.class).toList();
		assertEquals(2, tablesFromObject.size());

		var parentTableRows = DocumentUtil.streamElements(tablesFromObject.get(0), Tr.class).toList();
		// gets all the rows within the table and the nested table
		assertEquals(5, parentTableRows.size());

		var nestedTableRows = DocumentUtil.streamElements(tablesFromObject.get(1), Tr.class).toList();
		assertEquals(2, nestedTableRows.size());

		var parentTableCells = DocumentUtil.streamElements(tablesFromObject.get(0), Tc.class).toList();
		// gets all the cells within the table and the nested table
		assertEquals(5, parentTableCells.size());

		assertEquals("This row stays untouched.", getTextFromCell(parentTableCells.get(0)));
		assertEquals("This row stays untouched.", getTextFromCell(parentTableCells.get(1)));
		assertEquals("Also works on nested Tables", getTextFromCell(parentTableCells.get(3)));
		assertEquals("This row stays untouched.", getTextFromCell(parentTableCells.get(4)));
	}

	private String getTextFromCell(Tc tc) {
		List<P> paragraphsFromObject = DocumentUtil.streamElements(tc, P.class).toList();
		assertEquals(1, paragraphsFromObject.size());
		return TextUtils.getText(paragraphsFromObject.get(0));
	}

	public record Name(String name) {
	}
}
