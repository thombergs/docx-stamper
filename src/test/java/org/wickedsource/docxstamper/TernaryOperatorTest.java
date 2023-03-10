package org.wickedsource.docxstamper;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.wml.P;
import org.junit.jupiter.api.Test;
import org.wickedsource.docxstamper.util.ParagraphWrapper;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TernaryOperatorTest {
	@Test
	public void test() throws Docx4JException, IOException {
		var context = new Name("Homer");
		var template = getClass().getResourceAsStream("TernaryOperatorTest.docx");
		var stamper = new TestDocxStamper<Name>();
		var document = stamper.stampAndLoad(template, context);
		var nameParagraph = (P) document.getMainDocumentPart().getContent().get(3);
		assertEquals("Homer <-- this should read \"Homer\".", new ParagraphWrapper(nameParagraph).getText());
		var fooParagraph = (P) document.getMainDocumentPart().getContent().get(4);
		assertEquals("<-- this should be empty.", new ParagraphWrapper(fooParagraph).getText().trim());
	}

	@Test
	public void test2() throws IOException, Docx4JException {
		var context = new Name("Homer");
		var template = getClass().getResourceAsStream("TernaryOperatorTest2.docx");
		var stamper = new TestDocxStamper<Name>();
		var document = stamper.stampAndLoad(template, context);

		P firstParagraph = (P) document.getMainDocumentPart().getContent().get(1);
		assertEquals("Text before replacement ", new ParagraphWrapper(firstParagraph).getText());

		P secondParagraph = (P) document.getMainDocumentPart().getContent().get(2);
		assertEquals("replacement Text after", new ParagraphWrapper(secondParagraph).getText());

		P thirdParagraph = (P) document.getMainDocumentPart().getContent().get(3);
		assertEquals("Text before replacement Text after", new ParagraphWrapper(thirdParagraph).getText());
	}

	public record Name(String name) {
	}
}