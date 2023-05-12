package org.wickedsource.docxstamper;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.junit.jupiter.api.Test;
import org.wickedsource.docxstamper.util.ParagraphWrapper;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExpressionReplacementInGlobalParagraphsTest {
	@Test
	public void test() throws Docx4JException, IOException {
		var context = new Name("Homer Simpson");
		var template = getClass().getResourceAsStream("ExpressionReplacementInGlobalParagraphsTest.docx");
		var stamper = new TestDocxStamper<Name>(new DocxStamperConfiguration().setFailOnUnresolvedExpression(false));
		var document = stamper.stampAndLoad(template, context);
		resolvedExpressionsAreReplaced(document);
		unresolvedExpressionsAreNotReplaced(document);
	}

	private void resolvedExpressionsAreReplaced(WordprocessingMLPackage document) {
		P nameParagraph = (P) document.getMainDocumentPart().getContent().get(2);
		assertEquals("In this paragraph, the variable name should be resolved to the value Homer Simpson.",
					 new ParagraphWrapper(nameParagraph).getText());
	}

	private void unresolvedExpressionsAreNotReplaced(WordprocessingMLPackage document) {
		P fooParagraph = (P) document.getMainDocumentPart().getContent().get(3);
		assertEquals("In this paragraph, the variable foo should not be resolved: ${foo}.",
					 new ParagraphWrapper(fooParagraph).getText());
	}

	public record Name(String name) {
	}
}
