package org.wickedsource.docxstamper;

import org.junit.jupiter.api.Test;
import org.wickedsource.docxstamper.api.UnresolvedExpressionException;
import org.wickedsource.docxstamper.context.Name;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class FailOnUnresolvedExpressionTest extends AbstractDocx4jTest {

	@Test
	public void fails() {
		Name context = new Name("Homer");
		InputStream template = getClass().getResourceAsStream("FailOnUnresolvedExpressionTest.docx");
		DocxStamper<Name> stamper = new DocxStamper<>();
		assertThrows(UnresolvedExpressionException.class,
					 () -> stamper.stamp(template, context, new ByteArrayOutputStream()));
	}

	@Test
	public void doesNotFail() {
		Name context = new Name("Homer");
		InputStream template = getClass().getResourceAsStream("FailOnUnresolvedExpressionTest.docx");
		DocxStamper<Name> stamper = new DocxStamperConfiguration()
				.setFailOnUnresolvedExpression(false)
				.build();
		stamper.stamp(template, context, new ByteArrayOutputStream());
		// no exception
	}

}
