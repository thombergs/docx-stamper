package org.wickedsource.docxstamper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import org.docx4j.Docx4J;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.junit.Assert;
import org.junit.Test;
import org.wickedsource.docxstamper.context.NameContext;

public class ExpressionReplacementWithDifferentFormattingSameParagraphTest extends AbstractDocx4jTest {

	@Test
	public void test() throws Docx4JException, IOException {
		NameContext context = new NameContext();
		context.setName("Homer Simpson");
		InputStream template = getClass().getResourceAsStream("ExpressionReplacementWithDifferentFormattingSameParagraphTest.docx");
		WordprocessingMLPackage document = stampAndLoad(template, context);
		Docx4J.save(document, new File("D:\\testFormat.docx"));
		assertBoldStyle((R) ((P) document.getMainDocumentPart().getContent().get(2)).getContent().get(1));
		assertItalicStyle((R) ((P) document.getMainDocumentPart().getContent().get(3)).getContent().get(1));
		assertBoldStyle((R) ((P) document.getMainDocumentPart().getContent().get(4)).getContent().get(4));
		assertHeaderStyle((P) document.getMainDocumentPart().getContent().get(5));
		assertBoldStyle((R) ((P) document.getMainDocumentPart().getContent().get(6)).getContent().get(1));

	}

	private void assertBoldStyle(R run) {
		Assert.assertTrue("expected Run to be styled bold!", run.getRPr().getB().isVal());
	}

	private void assertItalicStyle(R run) {
		Assert.assertTrue("expected Run to be styled italic!", run.getRPr().getI().isVal());
	}

	private void assertHeaderStyle(P p) {

		Assert.assertEquals("berschrift2", p.getPPr().getPStyle().getVal());
	}


}
