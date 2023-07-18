package org.wickedsource.docxstamper;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.wml.P;
import org.junit.jupiter.api.Test;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.TypedValue;
import org.wickedsource.docxstamper.util.ParagraphWrapper;
import pro.verron.docxstamper.utils.TestDocxStamper;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CustomEvaluationContextConfigurerTest {

	@Test
	void customEvaluationContextConfigurerIsHonored() throws Docx4JException, IOException {
		var config = new DocxStamperConfiguration();
		config.setEvaluationContextConfigurer(context -> context.addPropertyAccessor(new SimpleGetter("foo", "bar")));

		var template = getClass().getResourceAsStream("CustomEvaluationContextConfigurerTest.docx");
		var stamper = new TestDocxStamper<EmptyContext>(config);
		var document = stamper.stampAndLoad(template, new EmptyContext());

		var p2 = (P) document.getMainDocumentPart().getContent().get(2);
		assertEquals("The variable foo has the value bar.", new ParagraphWrapper(p2).getText());
	}

	static class EmptyContext {
	}

	static class SimpleGetter implements PropertyAccessor {

		private final String fieldName;

		private final Object value;

		public SimpleGetter(String fieldName, Object value) {
			this.fieldName = fieldName;
			this.value = value;
		}

		@Override
		public Class<?>[] getSpecificTargetClasses() {
			return null;
		}

		@Override
		public boolean canRead(EvaluationContext context, Object target, String name) {
			return true;
		}

		@Override
		public TypedValue read(EvaluationContext context, Object target, String name) {
			if (name.equals(this.fieldName)) {
				return new TypedValue(value);
			} else {
				return null;
			}
		}

		@Override
		public boolean canWrite(EvaluationContext context, Object target, String name) {
			return false;
		}

		@Override
		public void write(EvaluationContext context, Object target, String name, Object newValue) {
		}
	}
}
