package org.wickedsource.docxstamper;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.junit.jupiter.api.Test;
import org.wickedsource.docxstamper.context.DateContext;
import org.wickedsource.docxstamper.util.ParagraphWrapper;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DateReplacementTest extends AbstractDocx4jTest {
	@Test
	public void test() throws Docx4JException, IOException {
		Date now = new Date();
		DateContext context = new DateContext();
		context.setDate(now);

		InputStream template = getClass().getResourceAsStream("DateReplacementTest.docx");
		WordprocessingMLPackage document = stampAndLoad(template, context);

		ParagraphWrapper p = new ParagraphWrapper(((P) document.getMainDocumentPart().getContent().get(1)));

		assertEquals("Today is: " + new SimpleDateFormat("dd.MM.yyyy").format(now), p.getText());
	}
}
