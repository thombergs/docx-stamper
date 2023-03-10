package org.wickedsource.docxstamper;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;

public class ExpressionReplacementWithFormattingTest {

	@Test
	public void test() throws Docx4JException, IOException {
		var context = new Name("Homer Simpson");
		var template = getClass().getResourceAsStream("ExpressionReplacementWithFormattingTest.docx");
		var stamper = new TestDocxStamper<Name>();
		var actual = stamper.stampAndLoadAndExtract(template, context);
		var expected = List.of(
				" Expression Replacement with text format",
				"The text format should be kept intact when an expression is replaced.",
				"It should be bold: |Homer Simpson/b=true|",
				"It should be italic: |Homer Simpson/i=true|",
				"It should be superscript: |Homer Simpson/vertAlign=superscript|",
				"It should be subscript: |Homer Simpson/vertAlign=subscript|",
				"It should be striked: |Homer Simpson/strike=true|",
				"It should be underlined: |Homer Simpson/u=single|",
				"It should be doubly underlined: |Homer Simpson/u=double|",
				"It should be thickly underlined: |Homer Simpson/u=thick|",
				"It should be dot underlined: |Homer Simpson/u=dotted|",
				"It should be dash underlined: |Homer Simpson/u=dash|",
				"It should be dot and dash underlined: |Homer Simpson/u=dotDash|",
				"It should be dot, dot and dash underlined: |Homer Simpson/u=dotDotDash|",
				"It should be highlighted yellow: |Homer Simpson/highlight=yellow|",
				"It should be white over darkblue: |Homer Simpson/color=FFFFFF,highlight=darkBlue|",
				"It should be with header formatting: |Homer Simpson/rStyle=TitreCar|");
		assertIterableEquals(expected, actual);
	}

	public record Name(String name) {
	}
}
