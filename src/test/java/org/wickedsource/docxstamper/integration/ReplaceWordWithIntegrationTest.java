package org.wickedsource.docxstamper.integration;

import org.junit.jupiter.api.Test;
import pro.verron.docxstamper.utils.TestDocxStamper;

import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;

class ReplaceWordWithIntegrationTest {

	@Test
    void test() {
		String name = "Simpsons";
		Name context = new Name(name);
		InputStream template = getClass().getResourceAsStream("ReplaceWordWithIntegrationTest.docx");
		var stamper = new TestDocxStamper<Name>();
		var actual = stamper.stampAndLoadAndExtract(template, context);
		var expected = List.of(
				"ReplaceWordWith Integration//rPr={}",
				"This variable |name/b=true|| /b=true|should be resolved to the value Simpsons.//rPr={b=true}",
				"This variable |name/b=true| should be resolved to the value Simpsons.//rPr={}",
				"//rPr={}");
		assertIterableEquals(expected, actual);
	}

	public record Name(String name) {
	}
}