package org.wickedsource.docxstamper;

import jakarta.xml.bind.JAXBElement;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Drawing;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.junit.jupiter.api.Test;
import org.wickedsource.docxstamper.context.ImageContext;
import org.wickedsource.docxstamper.replace.typeresolver.image.Image;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ImageReplacementInGlobalParagraphsTest extends AbstractDocx4jTest {

	@Test
	public void test() throws Docx4JException, IOException {
		Image monalisa = new Image(getClass().getResourceAsStream("monalisa.jpg"));
		ImageContext context = new ImageContext();
		context.setMonalisa(monalisa);

		InputStream template = getClass().getResourceAsStream("ImageReplacementInGlobalParagraphsTest.docx");
		WordprocessingMLPackage document = stampAndLoad(template, context);

		assertTrue(((JAXBElement<?>) ((R) ((P) document.getMainDocumentPart().getContent().get(2)).getContent()
																								  .get(1)).getContent()
																										  .get(0)).getValue() instanceof Drawing);
		assertTrue(((JAXBElement<?>) ((R) ((P) document.getMainDocumentPart().getContent().get(3)).getContent()
																								  .get(1)).getContent()
																										  .get(0)).getValue() instanceof Drawing);

	}

	@Test
	public void testWithMaxWidth() throws Docx4JException, IOException {
		Image monalisa = new Image(getClass().getResourceAsStream("monalisa.jpg"), 1000);
		ImageContext context = new ImageContext();
		context.setMonalisa(monalisa);

		InputStream template = getClass().getResourceAsStream("ImageReplacementInGlobalParagraphsTest.docx");
		WordprocessingMLPackage document = stampAndLoad(template, context);

		assertTrue(((JAXBElement<?>) ((R) ((P) document.getMainDocumentPart().getContent().get(2)).getContent()
																								  .get(1)).getContent()
																										  .get(0)).getValue() instanceof Drawing);
		assertTrue(((JAXBElement<?>) ((R) ((P) document.getMainDocumentPart().getContent().get(3)).getContent()
																								  .get(1)).getContent()
																										  .get(0)).getValue() instanceof Drawing);
	}
}