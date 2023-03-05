package org.wickedsource.docxstamper.integration;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.junit.jupiter.api.Test;
import org.wickedsource.docxstamper.AbstractDocx4jTest;
import org.wickedsource.docxstamper.DocxStamper;
import org.wickedsource.docxstamper.DocxStamperConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReplaceWordWithIntegrationTest extends AbstractDocx4jTest {

	@Test
	public void test() throws Docx4JException, IOException {
		String name = "Simpsons";
		Name context = new Name(name);
		InputStream template = getClass().getResourceAsStream("ReplaceWordWithIntegrationTest.docx");
		OutputStream out = getOutputStream();
		DocxStamper<Name> stamper = new DocxStamperConfiguration()
				.setFailOnUnresolvedExpression(false)
				.build();
		stamper.stamp(template, context, out);
		List<String> actual = extractDocumentParagraphs(out);
		List<Object> expected = List.of(
				"ReplaceWordWith Integration",
				"This variable name should be resolved to the value Simpsons.",
				"This variable name should be resolved to the value Simpsons.",
				"");
		assertEquals(expected, actual);
	}

	public record Name(String name) {
	}
}