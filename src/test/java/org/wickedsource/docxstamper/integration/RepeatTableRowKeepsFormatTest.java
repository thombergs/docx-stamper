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

public class RepeatTableRowKeepsFormatTest extends AbstractDocx4jTest {
	@Test
	public void test() throws Docx4JException, IOException {
		Show context = new Show(List.of(
				new Character(1, "st", "Homer Simpson", "Dan Castellaneta"),
				new Character(2, "nd", "Marge Simpson", "Julie Kavner"),
				new Character(3, "rd", "Bart Simpson", "Nancy Cartwright"),
				new Character(4, "th", "Lisa Simpson", "Yeardley Smith"),
				new Character(5, "th", "Maggie Simpson", "Julie Kavner")
		));
		InputStream template = getClass().getResourceAsStream("RepeatTableRowKeepsFormatTest.docx");
		OutputStream out = getOutputStream();
		DocxStamper<Show> stamper = new DocxStamperConfiguration()
				.setFailOnUnresolvedExpression(false)
				.build();
		stamper.stamp(template, context, out);
		var actual = extractDocumentParagraphs(out);
		var expected = List.of(
				List.of("1", "st(vertAlign=superscript)", " ", "Homer Simpson", "-", "Dan Castellaneta(b=true)"),
				List.of("2", "nd(vertAlign=superscript)", " ", "Marge Simpson", "-", "Julie Kavner(b=true)"),
				List.of("3", "rd(vertAlign=superscript)", " ", "Bart Simpson", "-", "Nancy Cartwright(b=true)"),
				List.of("4", "th(vertAlign=superscript)", " ", "Lisa Simpson", "-", "Yeardley Smith(b=true)"),
				List.of("5", "th(vertAlign=superscript)", " ", "Maggie Simpson", "-", "Julie Kavner(b=true)"),
				List.of());
		assertEquals(expected, actual);
	}

	public record Show(List<Character> characters) {
	}

	public record Character(int index, String indexSuffix, String characterName, String actorName) {
	}
}