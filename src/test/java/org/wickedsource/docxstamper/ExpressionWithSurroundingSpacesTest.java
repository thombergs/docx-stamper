package org.wickedsource.docxstamper;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.wml.P;
import org.junit.jupiter.api.Test;
import org.wickedsource.docxstamper.util.ParagraphWrapper;
import pro.verron.docxstamper.utils.TestDocxStamper;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExpressionWithSurroundingSpacesTest {

	@Test
	public void test() throws Docx4JException, IOException {
		var context = new Context();
		var template = getClass().getResourceAsStream("ExpressionWithSurroundingSpacesTest.docx");
		var stamper = new TestDocxStamper<Context>();
		var document = stamper.stampAndLoad(template, context);

		assertEquals("Before Expression After.",
					 new ParagraphWrapper((P) document.getMainDocumentPart().getContent().get(2)).getText());
		assertEquals("Before Expression After.",
					 new ParagraphWrapper((P) document.getMainDocumentPart().getContent().get(3)).getText());
		assertEquals("Before Expression After.",
					 new ParagraphWrapper((P) document.getMainDocumentPart().getContent().get(4)).getText());
		assertEquals("Before Expression After.",
					 new ParagraphWrapper((P) document.getMainDocumentPart().getContent().get(5)).getText());
		assertEquals("Before Expression After.",
					 new ParagraphWrapper((P) document.getMainDocumentPart().getContent().get(6)).getText());
		assertEquals("Before Expression After.",
					 new ParagraphWrapper((P) document.getMainDocumentPart().getContent().get(7)).getText());
		assertEquals("Before Expression After.",
					 new ParagraphWrapper((P) document.getMainDocumentPart().getContent().get(8)).getText());
	}

	static class Context {
		private final String expressionWithLeadingAndTrailingSpace = " Expression ";
		private final String expressionWithLeadingSpace = " Expression";
		private final String expressionWithTrailingSpace = "Expression ";
		private final String expressionWithoutSpaces = "Expression";

		public String getExpressionWithLeadingAndTrailingSpace() {
			return " Expression ";
		}

		public String getExpressionWithLeadingSpace() {
			return " Expression";
		}

		public String getExpressionWithTrailingSpace() {
			return "Expression ";
		}

		public String getExpressionWithoutSpaces() {
			return "Expression";
		}
	}
}
