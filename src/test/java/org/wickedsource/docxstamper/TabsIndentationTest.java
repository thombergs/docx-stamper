package org.wickedsource.docxstamper;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.wml.P;
import org.junit.jupiter.api.Test;
import org.wickedsource.docxstamper.util.ParagraphWrapper;
import pro.verron.docxstamper.utils.TestDocxStamper;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TabsIndentationTest {
	@Test
	public void tabsArePreserved() throws Docx4JException, IOException {
		var context = new Name("Homer Simpson");
		var template = getClass().getResourceAsStream("TabsIndentationTest.docx");
		var stamper = new TestDocxStamper<Name>();
		var document = stamper.stampAndLoad(template, context);

		var nameParagraph = (P) document.getMainDocumentPart().getContent().get(0);
		assertEquals("Tab\tHomer Simpson", new ParagraphWrapper(nameParagraph).getText());
	}

	@Test
	public void whiteSpacesArePreserved() throws Docx4JException, IOException {
		var context = new Name("Homer Simpson");
		var template = getClass().getResourceAsStream("TabsIndentationTest.docx");
		var stamper = new TestDocxStamper<Name>();
		var document = stamper.stampAndLoad(template, context);

		var nameParagraph = (P) document.getMainDocumentPart().getContent().get(1);
		assertEquals("Space Homer Simpson", new ParagraphWrapper(nameParagraph).getText());
	}

	public record Name(String name) {
	}


}