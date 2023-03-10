package org.wickedsource.docxstamper.integration;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.junit.jupiter.api.Test;
import org.wickedsource.docxstamper.TestDocxStamper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;

public class ReplaceWordWithIntegrationTest {

	@Test
	public void test() throws Docx4JException, IOException {
		String name = "Simpsons";
		Name context = new Name(name);
		InputStream template = getClass().getResourceAsStream("ReplaceWordWithIntegrationTest.docx");
		var stamper = new TestDocxStamper<Name>();
		var actual = stamper.stampAndLoadAndExtract(template, context);
		var expected = List.of(
				"ReplaceWordWith Integration",
				"This variable |name/b=true|| /b=true|should be resolved to the value Simpsons.",
				"This variable |name/b=true| should be resolved to the value Simpsons.",
				"");
		assertIterableEquals(expected, actual);
	}

	public record Name(String name) {
	}
}