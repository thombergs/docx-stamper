package org.wickedsource.docxstamper;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.wml.P;
import org.junit.jupiter.api.Test;
import org.wickedsource.docxstamper.replace.typeresolver.AbstractToTextResolver;
import org.wickedsource.docxstamper.util.ParagraphWrapper;
import pro.verron.docxstamper.utils.TestDocxStamper;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CustomTypeResolverTest {

	@Test
	public void test() throws Docx4JException, IOException {
		var resolver = new CustomTypeResolver();
		var config = new DocxStamperConfiguration()
				.addTypeResolver(CustomType.class, resolver);
		var template = getClass().getResourceAsStream("CustomTypeResolverTest.docx");
		var stamper = new TestDocxStamper<Context>(config);
		var document = stamper.stampAndLoad(template, new Context());
		var nameParagraph = (P) document.getMainDocumentPart().getContent().get(2);
		assertEquals("The name should be resolved to foo.", new ParagraphWrapper(nameParagraph).getText());
	}

	public static class Context {
		private CustomType name = new CustomType();

		public CustomType getName() {
			return name;
		}

		public void setName(CustomType name) {
			this.name = name;
		}
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
