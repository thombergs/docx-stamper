package org.wickedsource.docxstamper.integration;

import org.junit.jupiter.api.Test;
import pro.verron.docxstamper.utils.TestDocxStamper;

import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;

class RepeatTableRowKeepsFormatTest {
	@Test
	void test() {
		Show context = new Show(List.of(
				new Character(1, "st", "Homer Simpson", "Dan Castellaneta"),
				new Character(2, "nd", "Marge Simpson", "Julie Kavner"),
				new Character(3, "rd", "Bart Simpson", "Nancy Cartwright"),
				new Character(4, "th", "Lisa Simpson", "Yeardley Smith"),
				new Character(5, "th", "Maggie Simpson", "Julie Kavner")
		));
		InputStream template = getClass().getResourceAsStream("RepeatTableRowKeepsFormatTest.docx");
		var stamper = new TestDocxStamper<Show>();
		var actual = stamper.stampAndLoadAndExtract(template, context);
		var expected = List.of(
				"1|st/vertAlign=superscript| Homer Simpson-|Dan Castellaneta/b=true|",
				"2|nd/vertAlign=superscript| Marge Simpson-|Julie Kavner/b=true|",
				"3|rd/vertAlign=superscript| Bart Simpson-|Nancy Cartwright/b=true|",
				"4|th/vertAlign=superscript| Lisa Simpson-|Yeardley Smith/b=true|",
				"5|th/vertAlign=superscript| Maggie Simpson-|Julie Kavner/b=true|",
				"");
		assertIterableEquals(expected, actual);
	}

	public record Show(List<Character> characters) {
	}

	public record Character(int index, String indexSuffix, String characterName, String actorName) {
	}
}