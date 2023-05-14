package org.wickedsource.docxstamper;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.wml.P;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Tr;
import org.junit.jupiter.api.Test;
import org.wickedsource.docxstamper.context.Character;
import org.wickedsource.docxstamper.context.CharactersContext;
import org.wickedsource.docxstamper.util.DocumentUtil;
import org.wickedsource.docxstamper.util.ParagraphWrapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RepeatTableRowTest {
	@Test
	public void test() throws Docx4JException, IOException {
		var context = new CharactersContext();
		context.getCharacters().add(new Character("Homer Simpson", "Dan Castellaneta"));
		context.getCharacters().add(new Character("Marge Simpson", "Julie Kavner"));
		context.getCharacters().add(new Character("Bart Simpson", "Nancy Cartwright"));
		context.getCharacters().add(new Character("Kent Brockman", "Harry Shearer"));
		context.getCharacters().add(new Character("Disco Stu", "Hank Azaria"));
		context.getCharacters().add(new Character("Krusty the Clown", "Dan Castellaneta"));
		InputStream template = getClass().getResourceAsStream("RepeatTableRowTest.docx");

		var stamper = new TestDocxStamper<CharactersContext>();
		var document = stamper.stampAndLoad(template, context);

		var rows = DocumentUtil.getTableRowsFromObject(document);
		var tablesFromObject = DocumentUtil.streamElements(document, Tbl.class).toList();
		assertEquals(1, tablesFromObject.size());

		var parentTableRows = DocumentUtil.streamElements(tablesFromObject.get(0), Tr.class).toList();
		// 1 header row + 1 row per character in list
		assertEquals(7, rows.size());

		assertEquals(7, parentTableRows.size());

		final List<Tc> cells = DocumentUtil.getTableCellsFromObject(document);

		assertEquals("Homer Simpson", new ParagraphWrapper((P) cells.get(2).getContent().get(0)).getText());
		assertEquals("Dan Castellaneta", new ParagraphWrapper((P) cells.get(3).getContent().get(0)).getText());
		assertEquals("Marge Simpson", new ParagraphWrapper((P) cells.get(4).getContent().get(0)).getText());
		assertEquals("Julie Kavner", new ParagraphWrapper((P) cells.get(5).getContent().get(0)).getText());
		assertEquals("Bart Simpson", new ParagraphWrapper((P) cells.get(6).getContent().get(0)).getText());
		assertEquals("Nancy Cartwright", new ParagraphWrapper((P) cells.get(7).getContent().get(0)).getText());
		assertEquals("Kent Brockman", new ParagraphWrapper((P) cells.get(8).getContent().get(0)).getText());
		assertEquals("Harry Shearer", new ParagraphWrapper((P) cells.get(9).getContent().get(0)).getText());
		assertEquals("Disco Stu", new ParagraphWrapper((P) cells.get(10).getContent().get(0)).getText());
		assertEquals("Hank Azaria", new ParagraphWrapper((P) cells.get(11).getContent().get(0)).getText());
		assertEquals("Krusty the Clown", new ParagraphWrapper((P) cells.get(12).getContent().get(0)).getText());
		assertEquals("Dan Castellaneta", new ParagraphWrapper((P) cells.get(13).getContent().get(0)).getText());
	}
}