package org.wickedsource.docxstamper;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.io3.stores.PartStore;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.Drawing;
import org.junit.jupiter.api.Test;
import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.wickedsource.docxstamper.util.DocumentUtil;
import pro.verron.docxstamper.utils.TestDocxStamper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

		MainDocumentPart mainDocumentPart = document.getMainDocumentPart();
		List<Object> content = mainDocumentPart.getContent();

		assertEquals(content.size(), 8);
		ContentAccessor line2 = (ContentAccessor) content.get(2);
		ContentAccessor line2c0 = (ContentAccessor) line2.getContent().get(0);
		ContentAccessor line5 = (ContentAccessor) content.get(5);
		ContentAccessor line5c0 = (ContentAccessor) line5.getContent().get(0);

		assertEquals(content.get(0).toString(), "This is not repeated");
		assertEquals(content.get(1).toString(), "This should be repeated : first doc part");
		assertEquals(content.get(3).toString(), "This should be repeated too");
		assertEquals(content.get(4).toString(), "This should be repeated : second doc part");
		assertEquals(content.get(6).toString(), "This should be repeated too");
		assertEquals(content.get(7).toString(), "This is not repeated");

		Drawing image1 = (Drawing) XmlUtils.unwrap(line2c0.getContent().get(0));
		Drawing image2 = (Drawing) XmlUtils.unwrap(line5c0.getContent().get(0));

		String image1RelId = DocumentUtil.getImageRelationshipId(image1);
		String image2RelId = DocumentUtil.getImageRelationshipId(image2);

		RelationshipsPart relationshipsPart = mainDocumentPart.getRelationshipsPart();
		Part image1RelPart = relationshipsPart.getPart(image1RelId);
		Part image2RelPart = relationshipsPart.getPart(image2RelId);

		assertNotNull(image1RelPart);
		assertNotNull(image2RelPart);

		PartStore sourcePartStore = document.getSourcePartStore();
		String relPart1 = image1RelPart.getPartName().getName().substring(1);
		String relPart2 = image2RelPart.getPartName().getName().substring(1);
		assertNotEquals(0, sourcePartStore.getPartSize(relPart1));
		assertNotEquals(0, sourcePartStore.getPartSize(relPart2));
	}
}
