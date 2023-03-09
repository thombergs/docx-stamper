package org.wickedsource.docxstamper;

import jakarta.xml.bind.JAXBElement;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Tr;
import org.junit.jupiter.api.Test;
import org.wickedsource.docxstamper.util.ParagraphWrapper;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class ConditionalDisplayOfTablesBug32Test {
	@Test
	public void test() throws Docx4JException, IOException {
		var context = new Name("Homer");
		var template = getClass().getResourceAsStream("ConditionalDisplayOfTablesBug32Test.docx");
		var stamper = new TestDocxStamper<Name>();
		var document = stamper.stampAndLoad(template, context);
		globalTablesAreRemoved(document);
		nestedTablesAreRemoved(document);
	}

	private void globalTablesAreRemoved(WordprocessingMLPackage document) {
		assertTrue(document.getMainDocumentPart().getContent().get(1) instanceof P);
		P p1 = (P) document.getMainDocumentPart().getContent().get(1);
		Tbl table2 = (Tbl) ((JAXBElement<?>) document.getMainDocumentPart().getContent().get(3)).getValue();
		Tbl table3 = (Tbl) ((JAXBElement<?>) document.getMainDocumentPart().getContent().get(5)).getValue();
		P p4 = (P) document.getMainDocumentPart().getContent().get(7);

		assertEquals("This paragraph stays untouched.", new ParagraphWrapper(p1).getText());
		assertNotNull(table2);
		assertNotNull(table3);
		assertEquals("This paragraph stays untouched.", new ParagraphWrapper(p4).getText());
	}

	private void nestedTablesAreRemoved(WordprocessingMLPackage document) {
		Tbl outerTable = (Tbl) ((JAXBElement<?>) document.getMainDocumentPart().getContent().get(3)).getValue();
		Tc cell = (Tc) ((JAXBElement<?>) ((Tr) outerTable.getContent().get(1)).getContent().get(1)).getValue();
		P paragraph = (P) cell.getContent().get(0);
		String actual = new ParagraphWrapper(paragraph).getText();
		assertEquals("", actual); // empty paragraph, since the last element inside the cell was removed
	}

	public record Name(String name) {
	}
}