package org.wickedsource.docxstamper;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.junit.jupiter.api.Test;
import org.wickedsource.docxstamper.util.ParagraphWrapper;
import pro.verron.docxstamper.utils.TestDocxStamper;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LeaveEmptyOnExpressionErrorTest {
	@Test
	public void test() throws Docx4JException, IOException {
		var context = new Name("Homer Simpson");
		var template = getClass().getResourceAsStream("LeaveEmptyOnExpressionErrorTest.docx");
		var config = new DocxStamperConfiguration()
				.setFailOnUnresolvedExpression(false)
				.leaveEmptyOnExpressionError(true);
		var stamper = new TestDocxStamper<Name>(config);
		var document = stamper.stampAndLoad(template, context);
		resolvedExpressionsAreReplaced(document);
	}

	private void resolvedExpressionsAreReplaced(WordprocessingMLPackage document) {
		P nameParagraph = (P) document.getMainDocumentPart().getContent().get(0);
		assertEquals("Leave me empty .", new ParagraphWrapper(nameParagraph).getText());
	}

	public record Name(String name) {
	}
}
