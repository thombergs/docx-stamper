package org.wickedsource.docxstamper;

import jakarta.xml.bind.JAXBElement;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.wml.Drawing;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.junit.jupiter.api.Test;
import org.wickedsource.docxstamper.replace.typeresolver.image.Image;
import pro.verron.docxstamper.utils.TestDocxStamper;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ImageReplacementInGlobalParagraphsTest {

	/**
	 * <p>test.</p>
	 *
	 * @throws org.docx4j.openpackaging.exceptions.Docx4JException if any.
	 * @throws java.io.IOException                                 if any.
	 */
	@Test
    void test() throws Docx4JException, IOException {
		var context = new ImageContext(new Image(getClass().getResourceAsStream("monalisa.jpg")));
		var template = getClass().getResourceAsStream("ImageReplacementInGlobalParagraphsTest.docx");
		var stamper = new TestDocxStamper<ImageContext>();
		var document = stamper.stampAndLoad(template, context);
		assertTrue(((JAXBElement<?>) ((R) ((P) document.getMainDocumentPart().getContent().get(2)).getContent()
																								  .get(1)).getContent()
																										  .get(0)).getValue() instanceof Drawing);
		assertTrue(((JAXBElement<?>) ((R) ((P) document.getMainDocumentPart().getContent().get(3)).getContent()
																								  .get(1)).getContent()
																										  .get(0)).getValue() instanceof Drawing);
	}

	@Test
    void testWithMaxWidth() throws Docx4JException, IOException {
		var context = new ImageContext(new Image(getClass().getResourceAsStream("monalisa.jpg"), 1000));
		var template = getClass().getResourceAsStream("ImageReplacementInGlobalParagraphsTest.docx");
		var stamper = new TestDocxStamper<ImageContext>();
		var document = stamper.stampAndLoad(template, context);

		assertTrue(((JAXBElement<?>) ((R) ((P) document.getMainDocumentPart().getContent().get(2)).getContent()
																								  .get(1)).getContent()
																										  .get(0)).getValue() instanceof Drawing);
		assertTrue(((JAXBElement<?>) ((R) ((P) document.getMainDocumentPart().getContent().get(3)).getContent()
																								  .get(1)).getContent()
																										  .get(0)).getValue() instanceof Drawing);
	}

	public record ImageContext(Image monalisa) {
	}
}
