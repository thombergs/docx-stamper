package org.wickedsource.docxstamper;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.wml.P;
import org.junit.jupiter.api.Test;
import org.wickedsource.docxstamper.replace.typeresolver.AbstractToTextResolver;
import org.wickedsource.docxstamper.util.ParagraphWrapper;
import pro.verron.docxstamper.utils.TestDocxStamper;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CustomTypeResolverTest {

	@Test
    void test() throws Docx4JException, IOException {
		var resolver = new CustomTypeResolver();
		var config = new DocxStamperConfiguration()
				.addTypeResolver(CustomType.class, resolver);
		var template = getClass().getResourceAsStream("CustomTypeResolverTest.docx");
		var stamper = new TestDocxStamper<Context>(config);
		var document = stamper.stampAndLoad(template, new Context(new CustomType()));
		var nameParagraph = (P) document.getMainDocumentPart().getContent().get(2);
		assertEquals("The name should be resolved to foo.", new ParagraphWrapper(nameParagraph).getText());
	}

	record Context(CustomType name) {
	}

	public static class CustomType {
	}

	public static class CustomTypeResolver extends AbstractToTextResolver<CustomType> {
		@Override
		protected String resolveStringForObject(CustomType object) {
			return "foo";
		}
	}
}
