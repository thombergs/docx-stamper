package org.wickedsource.docxstamper;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.wml.*;
import org.junit.jupiter.api.Test;
import org.wickedsource.docxstamper.util.ParagraphWrapper;
import org.wickedsource.docxstamper.util.walk.BaseDocumentWalker;
import org.wickedsource.docxstamper.util.walk.DocumentWalker;
import pro.verron.docxstamper.utils.TestDocxStamper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RepeatDocPartTest {
	@Test
	public void test() throws Docx4JException, IOException {
		var context = new Characters();
		context.getCharacters().add(new Character("Homer Simpson", "Dan Castellaneta"));
		context.getCharacters().add(new Character("Marge Simpson", "Julie Kavner"));
		context.getCharacters().add(new Character("Bart Simpson", "Nancy Cartwright"));
		context.getCharacters().add(new Character("Kent Brockman", "Harry Shearer"));
		context.getCharacters().add(new Character("Disco Stu", "Hank Azaria"));
		context.getCharacters().add(new Character("Krusty the Clown", "Dan Castellaneta"));

		var template = getClass().getResourceAsStream("RepeatDocPartTest.docx");
		var stamper = new TestDocxStamper<Characters>();
		var document = stamper.stampAndLoad(template, context);

		var documentContent = document.getMainDocumentPart().getContent();

		int index = 2; // skip init paragraphs
		for (Character character : context.getCharacters()) {
			for (int j = 0; j < 3; j++) { // 3 elements should be repeated
				Object object = XmlUtils.unwrap(documentContent.get(index++));
				switch (j) {
					case 0 -> {
						P paragraph = (P) object;
						String expected = String.format("Paragraph for test: %s - %s",
														character.getName(),
														character.getActor());
						assertEquals(expected, new ParagraphWrapper(paragraph).getText());
					}
					case 1 -> {
						final List<Tc> cells = new ArrayList<>();
						DocumentWalker cellWalker = new BaseDocumentWalker((ContentAccessor) object) {
							@Override
							protected void onTableCell(Tc tableCell) {
								cells.add(tableCell);
							}
						};
						cellWalker.walk();

						assertEquals(character.getName(),
									 new ParagraphWrapper((P) cells.get(0).getContent().get(0)).getText());
						assertEquals(character.getActor(),
									 new ParagraphWrapper((P) cells.get(1).getContent().get(0)).getText());
					}
					case 2 -> {
						P paragraph = (P) object;
						List<Object> runs = paragraph.getContent();
						assertEquals(3, runs.size());

						List<Object> targetRunContent = ((R) runs.get(1)).getContent();
						assertEquals(1, targetRunContent.size());
						assertTrue(targetRunContent.get(0) instanceof Br);
					}
				}
			}
		}
	}

	public static class Character {

		private final String name;

		private final String actor;

		public Character(String name, String actor) {
			this.name = name;
			this.actor = actor;
		}

		public String getName() {
			return name;
		}

		public String getActor() {
			return actor;
		}
	}

	public class Characters {

		private final List<Character> characters = new ArrayList<>();

		public List<Character> getCharacters() {
			return characters;
		}
	}
}
