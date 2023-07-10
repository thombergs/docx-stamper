package org.wickedsource.docxstamper;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.wml.P;
import org.junit.jupiter.api.Test;
import org.wickedsource.docxstamper.util.ParagraphWrapper;
import pro.verron.docxstamper.utils.TestDocxStamper;
import pro.verron.docxstamper.utils.context.Contexts;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

public class TabsIndentationTest {
	@Test
	public void tabsArePreserved() throws Docx4JException, IOException {
		var context = Contexts.name("Homer Simpson");
		var template = getClass().getResourceAsStream("TabsIndentationTest.docx");
		var actual = new TestDocxStamper<>().stampAndLoadAndExtract(template, context);

		var expected = List.of(
				"|Tab/lang=en-US|||TAB|/lang=en-US||Homer Simpson/lang=en-US|//rPr={lang=en-US}",
				"|Space/lang=en-US|| /lang=en-US||Homer Simpson/lang=en-US|//rPr={lang=en-US}"
		);
		assertIterableEquals(expected, actual);
	}

	@Test
	public void whiteSpacesArePreserved() throws Docx4JException, IOException {
		var context = Contexts.name("Homer Simpson");
		var template = getClass().getResourceAsStream("TabsIndentationTest.docx");
		var actual = new TestDocxStamper<>().stampAndLoadAndExtract(template, context);

		var expected = List.of(
				"|Tab/lang=en-US|||TAB|/lang=en-US||Homer Simpson/lang=en-US|//rPr={lang=en-US}",
				"|Space/lang=en-US|| /lang=en-US||Homer Simpson/lang=en-US|//rPr={lang=en-US}"
		);
		assertIterableEquals(expected, actual);
	}
}