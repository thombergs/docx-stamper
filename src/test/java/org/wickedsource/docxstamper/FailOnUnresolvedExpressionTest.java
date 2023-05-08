package org.wickedsource.docxstamper;

import org.junit.jupiter.api.Test;
import org.wickedsource.docxstamper.api.UnresolvedExpressionException;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class FailOnUnresolvedExpressionTest {
	@Test
	public void fails() {
		var context = new Name("Homer");
		var template = getClass().getResourceAsStream("FailOnUnresolvedExpressionTest.docx");
		var stamper = new DocxStamper<Name>();
		assertThrows(UnresolvedExpressionException.class,
					 () -> stamper.stamp(template, context, new ByteArrayOutputStream()));
	}

	@Test
	public void doesNotFail() {
		Name context = new Name("Homer");
		InputStream template = getClass().getResourceAsStream("FailOnUnresolvedExpressionTest.docx");
		var config = new DocxStamperConfiguration()
				.setFailOnUnresolvedExpression(false);
		var stamper = new DocxStamper<Name>(config);
		stamper.stamp(template, context, new ByteArrayOutputStream());
		// no exception
	}

	public record Name(String name) {
	}

}
