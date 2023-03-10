package org.wickedsource.docxstamper;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.Drawing;
import org.junit.jupiter.api.Test;
import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.wickedsource.docxstamper.util.DocumentUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class RepeatDocPartWithImagesInSourceTest {
	@Test
	public void shouldReplicateImageFromTheMainDocumentInTheSubTemplate() throws Docx4JException, IOException {
		var context = new HashMap<String, Object>();
		var subDocParts = new ArrayList<Map<String, Object>>();

		var firstPart = new HashMap<String, Object>();
		firstPart.put("name", "first doc part");
		subDocParts.add(firstPart);

		var secondPart = new HashMap<String, Object>();
		secondPart.put("name", "second doc part");
		subDocParts.add(secondPart);

		context.put("subDocParts", subDocParts);

		var config = new DocxStamperConfiguration()
				.setEvaluationContextConfigurer((StandardEvaluationContext ctx) -> ctx.addPropertyAccessor(new MapAccessor()));

		var template = getClass().getResourceAsStream("RepeatDocPartWithImagesInSourceTest.docx");
		var stamper = new TestDocxStamper<Map<String, Object>>(config);
		var document = stamper.stampAndLoad(template, context);

		assertEquals(document.getMainDocumentPart().getContent().size(), 11);

		assertEquals(document.getMainDocumentPart().getContent().get(0).toString(), "This is not repeated");
		assertEquals(document.getMainDocumentPart().getContent().get(2).toString(),
					 "This should be repeated : first doc part");
		assertEquals(document.getMainDocumentPart().getContent().get(4).toString(),
					 "This should be repeated too");
		assertEquals(document.getMainDocumentPart().getContent().get(5).toString(),
					 "This should be repeated : second doc part");
		assertEquals(document.getMainDocumentPart().getContent().get(7).toString(),
					 "This should be repeated too");
		assertEquals(document.getMainDocumentPart().getContent().get(9).toString(), "Not this");

		Drawing image1 = (Drawing) XmlUtils.unwrap(((ContentAccessor) ((ContentAccessor) document.getMainDocumentPart()
																								 .getContent()
																								 .get(3)).getContent()
																										 .get(0)).getContent()
																												 .get(0));
		Drawing image2 = (Drawing) XmlUtils.unwrap(((ContentAccessor) ((ContentAccessor) document.getMainDocumentPart()
																								 .getContent()
																								 .get(6)).getContent()
																										 .get(0)).getContent()
																												 .get(0));

		String image1RelId = DocumentUtil.getImageRelationshipId(image1);
		String image2RelId = DocumentUtil.getImageRelationshipId(image2);

		Part image1RelPart = document.getMainDocumentPart().getRelationshipsPart().getPart(image1RelId);
		Part image2RelPart = document.getMainDocumentPart().getRelationshipsPart().getPart(image2RelId);

		assertNotNull(image1RelPart);
		assertNotNull(image2RelPart);

		assertNotEquals(document.getSourcePartStore()
								.getPartSize(image1RelPart.getPartName().getName().substring(1)), 0);
		assertNotEquals(document.getSourcePartStore()
								.getPartSize(image2RelPart.getPartName().getName().substring(1)), 0);
	}
}
