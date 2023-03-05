package org.wickedsource.docxstamper;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.junit.jupiter.api.Test;
import org.wickedsource.docxstamper.util.ParagraphUtil;
import org.wickedsource.docxstamper.util.ParagraphWrapper;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExpressionReplacementInTextBoxesTest extends AbstractDocx4jTest {
	@Test
	public void test() throws Docx4JException, IOException {
		Name context = new Name("Bart Simpson");
		InputStream template = getClass().getResourceAsStream("ExpressionReplacementInTextBoxesTest.docx");
		WordprocessingMLPackage document = stampAndLoad(template, context);
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