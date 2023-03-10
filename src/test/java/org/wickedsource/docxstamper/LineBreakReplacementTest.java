package org.wickedsource.docxstamper;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Br;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.junit.jupiter.api.Test;
import org.wickedsource.docxstamper.util.ParagraphWrapper;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LineBreakReplacementTest {
	@Test
	public void test() throws Docx4JException, IOException {
		var context = new Name(null);
		var config = new DocxStamperConfiguration();
		config.setLineBreakPlaceholder("#");
		var template = getClass().getResourceAsStream("LineBreakReplacementTest.docx");
		var stamper = new TestDocxStamper<Name>(config);
		var document = stamper.stampAndLoad(template, context);
		lineBreaksAreReplaced(document);
	}

	private void lineBreaksAreReplaced(WordprocessingMLPackage document) {
		P paragraph = (P) document.getMainDocumentPart().getContent().get(2);
		assertTrue(new ParagraphWrapper(paragraph).getText().contains("This paragraph should be"));
		assertTrue(new ParagraphWrapper(paragraph).getText().contains("split in"));
		assertTrue(new ParagraphWrapper(paragraph).getText().contains("three lines"));

		assertEquals(R.class, paragraph.getContent().get(1).getClass());
		assertEquals(Br.class, ((R) paragraph.getContent().get(1)).getContent().get(0).getClass());

		assertEquals(R.class, paragraph.getContent().get(3).getClass());
		assertEquals(Br.class, ((R) paragraph.getContent().get(3)).getContent().get(0).getClass());
	}

	public record Name(String name) {
	}

}
