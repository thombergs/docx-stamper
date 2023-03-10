package org.wickedsource.docxstamper;

import jakarta.xml.bind.JAXBElement;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Text;
import org.junit.jupiter.api.Test;
import org.springframework.context.expression.MapAccessor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MapAccessorAndReflectivePropertyAccessorTest {
	@Test
	public void shouldResolveMapAndPropertyPlaceholders() throws Docx4JException, IOException {
		var listProp = new ArrayList<Container>();
		listProp.add(new Container("first value"));
		listProp.add(new Container("second value"));

		var context = new HashMap<String, Object>();
		context.put("FLAT_STRING", "Flat string has been resolved");
		context.put("OBJECT_LIST_PROP", listProp);

		var config = new DocxStamperConfiguration()
				.setFailOnUnresolvedExpression(false)
				.setLineBreakPlaceholder("\n")
				.replaceNullValues(true)
				.nullValuesDefault("N/C")
				.replaceUnresolvedExpressions(true)
				.unresolvedExpressionsDefaultValue("N/C")
				.setEvaluationContextConfigurer(ctx -> ctx.addPropertyAccessor(new MapAccessor()));

		var template = getClass().getResourceAsStream("MapAccessorAndReflectivePropertyAccessorTest.docx");
		var stamper = new TestDocxStamper<Map<String, Object>>(config);
		var result = stamper.stampAndLoad(template, context);
		var contents = result.getMainDocumentPart().getContent();

		assertEquals("Flat string has been resolved",
					 ((Text) ((JAXBElement<?>) ((R) ((P) contents.get(0)).getContent().get(1)).getContent()
																							  .get(0)).getValue()).getValue());
		assertEquals(3, ((Tbl) ((JAXBElement<?>) contents.get(2)).getValue()).getContent().size());
		assertEquals("first value",
					 ((Text) ((JAXBElement<?>) ((R) ((P) contents.get(6)).getContent().get(0)).getContent()
																							  .get(0)).getValue()).getValue());
		assertEquals("second value",
					 ((Text) ((JAXBElement<?>) ((R) ((P) contents.get(9)).getContent().get(0)).getContent()
																							  .get(0)).getValue()).getValue());
	}

	static class Container {
		public String value;

		public Container(String value) {
			this.value = value;
		}
	}
}
