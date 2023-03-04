package org.wickedsource.docxstamper;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.junit.jupiter.api.Test;
import org.wickedsource.docxstamper.context.Name;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExpressionReplacementWithFormattingTest extends AbstractDocx4jTest {

	@Test
	public void test() throws Docx4JException, IOException {
		Name context = new Name("Homer Simpson");
		InputStream template = getClass().getResourceAsStream("ExpressionReplacementWithFormattingTest.docx");
		WordprocessingMLPackage document = stampAndLoad(template, context);

		assertBoldStyle((R) ((P) document.getMainDocumentPart().getContent().get(2)).getContent().get(1));
		assertItalicStyle((R) ((P) document.getMainDocumentPart().getContent().get(3)).getContent().get(1));
		assertBoldStyle((R) ((P) document.getMainDocumentPart().getContent().get(5)).getContent().get(1));

	}

	private void assertBoldStyle(R run) {
		assertTrue(run.getRPr().getB().isVal(), "expected Run to be styled bold!");
	}

	private void assertItalicStyle(R run) {
		assertTrue(run.getRPr().getI().isVal(), "expected Run to be styled italic!");
	}


}
