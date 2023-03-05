package org.wickedsource.docxstamper;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.junit.jupiter.api.Test;
import org.wickedsource.docxstamper.util.ParagraphWrapper;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TabsIndentationTest extends AbstractDocx4jTest {
	@Test
	public void tabsArePreserved() throws Docx4JException, IOException {
		Name context = new Name("Homer Simpson");
		InputStream template = getClass().getResourceAsStream("TabsIndentationTest.docx");
		WordprocessingMLPackage document = stampAndLoad(template, context);

		P nameParagraph = (P) document.getMainDocumentPart().getContent().get(0);
		assertEquals("Tab\tHomer Simpson", new ParagraphWrapper(nameParagraph).getText());
	}

	@Test
	public void whiteSpacesArePreserved() throws Docx4JException, IOException {
		Name context = new Name("Homer Simpson");
		InputStream template = getClass().getResourceAsStream("TabsIndentationTest.docx");
		WordprocessingMLPackage document = stampAndLoad(template, context);

		P nameParagraph = (P) document.getMainDocumentPart().getContent().get(1);
		assertEquals("Space Homer Simpson", new ParagraphWrapper(nameParagraph).getText());
	}

	public record Name(String name) {
	}


}