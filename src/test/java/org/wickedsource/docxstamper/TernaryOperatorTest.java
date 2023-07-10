package org.wickedsource.docxstamper;

import org.junit.jupiter.api.Test;
import pro.verron.docxstamper.utils.TestDocxStamper;
import pro.verron.docxstamper.utils.context.Contexts;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;

public class TernaryOperatorTest {
	@Test
	public void test() {
		var context = Contexts.name("Homer");
		var template = getClass().getResourceAsStream("TernaryOperatorTest.docx");

		var actual = new TestDocxStamper<>().stampAndLoadAndExtract(template, context);

		var expected = List.of(
				"Expression Replacement with ternary operator",
				"This paragraph is untouched.//rPr={}",
				"Some replacement before the ternary operator: Homer.//rPr={}",
				"Homer <-- this should read \"Homer\".//rPr={}",
				" <-- this should be empty.//rPr={}"
		);
		assertIterableEquals(expected, actual);
	}


}