package org.wickedsource.docxstamper;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.junit.jupiter.api.Test;
import org.wickedsource.docxstamper.util.ParagraphWrapper;
import pro.verron.docxstamper.utils.TestDocxStamper;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReplaceNullExpressionTest {
	@Test
	public void test() throws Docx4JException, IOException {
		var context = new Name(null);
		var template = getClass().getResourceAsStream("ReplaceNullExpressionTest.docx");
		var config = new DocxStamperConfiguration().replaceNullValues(true);
		var stamper = new TestDocxStamper<Name>(config);
		var document = stamper.stampAndLoad(template, context);
		checkNullValueIsReplaced(document);
	}

	private void checkNullValueIsReplaced(WordprocessingMLPackage document) {
		P nameParagraph = (P) document.getMainDocumentPart().getContent().get(0);
		assertEquals("I am .", new ParagraphWrapper(nameParagraph).getText());
	}

	public record Name(String name) {
	}
}
