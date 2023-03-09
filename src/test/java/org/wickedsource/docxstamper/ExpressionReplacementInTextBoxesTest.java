package org.wickedsource.docxstamper;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.junit.jupiter.api.Test;
import org.wickedsource.docxstamper.util.ParagraphUtil;
import org.wickedsource.docxstamper.util.ParagraphWrapper;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExpressionReplacementInTextBoxesTest {
	@Test
	public void test() throws Docx4JException, IOException {
		var context = new Name("Bart Simpson");
		var template = getClass().getResourceAsStream("ExpressionReplacementInTextBoxesTest.docx");
		var stamper = new TestDocxStamper<Name>();
		var document = stamper.stampAndLoad(template, context);
		resolvedExpressionsAreReplacedInFirstLevelTextBox(document);
		unresolvedExpressionsAreNotReplacedInFirstTextBox(document);
	}

	private void resolvedExpressionsAreReplacedInFirstLevelTextBox(WordprocessingMLPackage document) {
		P nameParagraph = (P) ParagraphUtil.getAllTextBoxes(document).get(0);
		assertEquals("Bart Simpson", new ParagraphWrapper(nameParagraph).getText());
	}

	private void unresolvedExpressionsAreNotReplacedInFirstTextBox(WordprocessingMLPackage document) {
		P nameParagraph = (P) ParagraphUtil.getAllTextBoxes(document).get(2);
		assertEquals("${foo}", new ParagraphWrapper(nameParagraph).getText());
	}

	public record Name(String name) {
	}
}