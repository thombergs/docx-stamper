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
				" Expression Replacement with text format//rPr={}",
				"The text format should be kept intact when an expression is replaced.//rPr={}",
				"It should be bold: |Homer Simpson/b=true|//rPr={b=true}",
				"It should be italic: |Homer Simpson/i=true|//rPr={i=true}",
				"It should be superscript: |Homer Simpson/vertAlign=superscript|//rPr={i=true}",
				"It should be subscript: |Homer Simpson/vertAlign=subscript|//rPr={vertAlign=subscript}",
				"It should be striked: |Homer Simpson/strike=true|//rPr={i=true}",
				"It should be underlined: |Homer Simpson/u=single|//rPr={i=true}",
				"It should be doubly underlined: |Homer Simpson/u=double|//rPr={i=true}",
				"It should be thickly underlined: |Homer Simpson/u=thick|//rPr={i=true}",
				"It should be dot underlined: |Homer Simpson/u=dotted|//rPr={i=true}",
				"It should be dash underlined: |Homer Simpson/u=dash|//rPr={i=true}",
				"It should be dot and dash underlined: |Homer Simpson/u=dotDash|//rPr={i=true}",
				"It should be dot, dot and dash underlined: |Homer Simpson/u=dotDotDash|//rPr={i=true}",
				"It should be highlighted yellow: |Homer Simpson/highlight=yellow|//rPr={}",
				"It should be white over darkblue: |Homer Simpson/color=FFFFFF,highlight=darkBlue|//rPr={b=true}",
				"It should be with header formatting: |Homer Simpson/rStyle=TitreCar|//rPr={b=true}");
		assertIterableEquals(expected, actual);
	}

	public record Name(String name) {
	}
}
