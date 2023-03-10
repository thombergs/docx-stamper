package org.wickedsource.docxstamper;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.junit.jupiter.api.Test;
import org.wickedsource.docxstamper.context.Character;
import org.wickedsource.docxstamper.context.CharactersContext;
import org.wickedsource.docxstamper.util.ParagraphWrapper;
import org.wickedsource.docxstamper.util.walk.BaseCoordinatesWalker;

import java.io.IOException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RepeatParagraphTest {

	@Test
	public void test() throws Docx4JException, IOException {
		var context = new CharactersContext();
		context.getCharacters().add(new Character("Homer Simpson", "Dan Castellaneta"));
		context.getCharacters().add(new Character("Marge Simpson", "Julie Kavner"));
		context.getCharacters().add(new Character("Bart Simpson", "Nancy Cartwright"));
		context.getCharacters().add(new Character("Kent Brockman", "Harry Shearer"));
		context.getCharacters().add(new Character("Disco Stu", "Hank Azaria"));
		context.getCharacters().add(new Character("Krusty the Clown", "Dan Castellaneta"));
		var template = getClass().getResourceAsStream("RepeatParagraphTest.docx");
		var stamper = new TestDocxStamper<CharactersContext>();
		WordprocessingMLPackage document = stamper.stampAndLoad(template, context);

		var titleCoords = new ArrayList<P>();
		var quotationCoords = new ArrayList<P>();
		var walker = new BaseCoordinatesWalker(document) {
			@Override
			protected void onParagraph(P paragraph) {
				if ("Titre2".equals(paragraph.getPPr().getPStyle().getVal())) {
					titleCoords.add(paragraph);
				} else if ("Quotations".equals(paragraph.getPPr().getPStyle().getVal())) {
					quotationCoords.add(paragraph);
				}
			}
		};
		walker.walk();

		// 6 titles and 6 quotations are expected
		assertEquals(6, titleCoords.size());
		assertEquals(6, quotationCoords.size());

		// Check paragraph's content
		assertEquals("Homer Simpson", new ParagraphWrapper(titleCoords.get(0)).getText());
		assertEquals("Dan Castellaneta", new ParagraphWrapper(quotationCoords.get(0)).getText());
		assertEquals("Marge Simpson", new ParagraphWrapper(titleCoords.get(1)).getText());
		assertEquals("Julie Kavner", new ParagraphWrapper(quotationCoords.get(1)).getText());
		assertEquals("Bart Simpson", new ParagraphWrapper(titleCoords.get(2)).getText());
		assertEquals("Nancy Cartwright", new ParagraphWrapper(quotationCoords.get(2)).getText());
		assertEquals("Kent Brockman", new ParagraphWrapper(titleCoords.get(3)).getText());
		assertEquals("Harry Shearer", new ParagraphWrapper(quotationCoords.get(3)).getText());
		assertEquals("Disco Stu", new ParagraphWrapper(titleCoords.get(4)).getText());
		assertEquals("Hank Azaria", new ParagraphWrapper(quotationCoords.get(4)).getText());
		assertEquals("Krusty the Clown", new ParagraphWrapper(titleCoords.get(5)).getText());
		assertEquals("Dan Castellaneta", new ParagraphWrapper(quotationCoords.get(5)).getText());
	}


}
