package org.wickedsource.docxstamper;

import org.docx4j.dml.wordprocessingDrawing.Anchor;
import org.junit.jupiter.api.Test;
import pro.verron.docxstamper.utils.TestDocxStamper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.wickedsource.docxstamper.DefaultTests.getResource;

class ExpressionReplacementInTextBoxesTest {
	@Test
    void expressionReplacementInTextBoxesTest() {
		var context = new Name("Bart Simpson");
		var template = getResource("ExpressionReplacementInTextBoxesTest" +
									 ".docx");
		var stamper = new TestDocxStamper<Name>(new DocxStamperConfiguration().setFailOnUnresolvedExpression(false));
		var actual = stamper.stampAndLoadAndExtract(template, context, Anchor.class);
		List<String> expected = List.of(
				"❬Bart Simpson❘color=auto❭",
				"❬${foo}❘color=auto❭"
		);
		assertIterableEquals(expected, actual);
	}

	public record Name(String name) {
	}
}
