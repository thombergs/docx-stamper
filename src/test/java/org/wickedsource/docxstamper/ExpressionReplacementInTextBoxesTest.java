package org.wickedsource.docxstamper;

import org.docx4j.dml.wordprocessingDrawing.Anchor;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;

public class ExpressionReplacementInTextBoxesTest {
	@Test
	public void test() throws Docx4JException, IOException {
		var context = new Name("Bart Simpson");
		var template = getClass().getResourceAsStream("ExpressionReplacementInTextBoxesTest.docx");
		var stamper = new TestDocxStamper<Name>();
		var actual = stamper.stampAndLoadAndExtract(template, context, Anchor.class);
		List<String> expected = List.of(
				"|Bart Simpson/color=auto|",
				"|${foo}/color=auto|"
		);
		assertIterableEquals(expected, actual);
	}

	public record Name(String name) {
	}
}