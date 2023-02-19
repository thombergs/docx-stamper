package org.wickedsource.docxstamper;

import org.docx4j.TextUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Tr;
import org.junit.jupiter.api.Test;
import org.wickedsource.docxstamper.context.NameContext;
import org.wickedsource.docxstamper.util.DocumentUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MultiStampTest extends AbstractDocx4jTest {

	@Test
	public void expressionsAreResolvedOnMultiStamp() throws Docx4JException, IOException {
		DocxStamper<NamesContext> stamper = new DocxStamper<>(new DocxStamperConfiguration().setFailOnUnresolvedExpression(
				false));
		NamesContext context = new NamesContext(List.of(new NameContext("Homer"),
														new NameContext("Marge"),
														new NameContext("Bart"),
														new NameContext("Lisa"),
														new NameContext("Maggie")));

		InputStream template = getClass().getResourceAsStream("MultiStampTest.docx");
		OutputStream out = getOutputStream();
		stamper.stamp(template, context, out);
		InputStream in = getInputStream(out);
		WordprocessingMLPackage document = WordprocessingMLPackage.load(in);
		assertTableRows(document);

		template = getClass().getResourceAsStream("MultiStampTest.docx");
		out = getOutputStream();
		stamper.stamp(template, context, out);
		in = getInputStream(out);
		document = WordprocessingMLPackage.load(in);
		assertTableRows(document);
	}

	private void assertTableRows(WordprocessingMLPackage document) {
		final List<Tbl> tablesFromObject = DocumentUtil.extractElements(document, Tbl.class);
		assertEquals(1, tablesFromObject.size());

		final List<Tr> tableRows = DocumentUtil.extractElements(tablesFromObject.get(0), Tr.class);
		assertEquals(5, tableRows.size());

		assertRowContainsText(tableRows.get(0), "Homer");
		assertRowContainsText(tableRows.get(1), "Marge");
		assertRowContainsText(tableRows.get(2), "Bart");
		assertRowContainsText(tableRows.get(3), "Lisa");
		assertRowContainsText(tableRows.get(4), "Maggie");
	}

	private static void assertRowContainsText(Tr row, String text) {
		final List<Tc> cell0 = DocumentUtil.extractElements(row, Tc.class);
		String cellContent = TextUtils.getText(cell0.get(0));
		String message = String.format("'%s' is not contained in '%s'", text, cellContent);
		assertTrue(cellContent.contains(text), message);
	}

	public record NamesContext(List<NameContext> names) {
	}
}