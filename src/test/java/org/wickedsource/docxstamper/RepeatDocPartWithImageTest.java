package org.wickedsource.docxstamper;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.junit.jupiter.api.Test;
import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.wickedsource.docxstamper.replace.typeresolver.image.Image;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RepeatDocPartWithImageTest {
	@Test
	public void shouldImportImageDataInTheMainDocument() throws Docx4JException, IOException {
		var images = new ArrayList<Image>();
		images.add(new Image(getClass().getResourceAsStream("butterfly.png")));
		images.add(new Image(getClass().getResourceAsStream("map.jpg")));

		var context = new HashMap<String, Object>();
		var units = new ArrayList<Map<String, Object>>();

		images.forEach(image -> {
			var unit = new HashMap<String, Object>();
			var productionFacility = new HashMap<String, Object>();
			unit.put("productionFacility", productionFacility);
			productionFacility.put("coverImage", image);
			units.add(unit);
		});

		context.put("units", units);

		var config = new DocxStamperConfiguration()
				.setEvaluationContextConfigurer((StandardEvaluationContext ctx) -> ctx.addPropertyAccessor(new MapAccessor()));

		var template = getClass().getResourceAsStream("RepeatDocPartWithImageTest.docx");

		var stamper = new TestDocxStamper<Map<String, Object>>(config);
		var document = stamper.stampAndLoad(template, context);

		assertEquals(images.get(0).getImageBytes().length,
					 document.getSourcePartStore().getPartSize("word/media/document_image_rId11.png"));
		assertEquals(images.get(1).getImageBytes().length,
					 document.getSourcePartStore().getPartSize("word/media/document_image_rId12.jpeg"));
		assertEquals(images.get(0).getImageBytes().length,
					 document.getSourcePartStore().getPartSize("word/media/document_image_rId13.png"));
	}
}
