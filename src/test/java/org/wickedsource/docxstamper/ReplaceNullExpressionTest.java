package org.wickedsource.docxstamper;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.junit.jupiter.api.Test;
import org.wickedsource.docxstamper.util.ParagraphWrapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReplaceNullExpressionTest extends AbstractDocx4jTest {
	@Test
	public void test() throws Docx4JException, IOException {
		Name context = new Name(null);
		InputStream template = getClass().getResourceAsStream("ReplaceNullExpressionTest.docx");
		OutputStream out = getOutputStream();
		DocxStamper<Name> stamper = new DocxStamperConfiguration()
				.replaceNullValues(true)
				.build();
		stamper.stamp(template, context, out);
		InputStream in = getInputStream(out);
		WordprocessingMLPackage document = WordprocessingMLPackage.load(in);
		checkNullValueIsReplaced(document);
	}

	private void checkNullValueIsReplaced(WordprocessingMLPackage document) {
		P nameParagraph = (P) document.getMainDocumentPart().getContent().get(0);
		assertEquals("I am .", new ParagraphWrapper(nameParagraph).getText());
	}

	public record Name(String name) {
	}
}
