package org.wickedsource.docxstamper;

import jakarta.xml.bind.JAXBElement;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Tr;
import org.junit.jupiter.api.Test;
import org.wickedsource.docxstamper.util.DocumentUtil;
import org.wickedsource.docxstamper.util.ParagraphWrapper;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class ConditionalDisplayOfParagraphsTest {
	@Test
	public void processorExpressionsInCommentsAreResolved() throws Docx4JException, IOException {
		var context = new Name("Homer");
		var template = getClass().getResourceAsStream("ConditionalDisplayOfParagraphsTest.docx");

		var stamper = new TestDocxStamper<Name>();
		var document = stamper.stampAndLoad(template, context);
		globalParagraphsAreRemoved(document);
		paragraphsInTableAreRemoved(document);
		paragraphsInNestedTablesAreRemoved(document);
	}

	private void globalParagraphsAreRemoved(WordprocessingMLPackage document) {
		P p1 = (P) document.getMainDocumentPart().getContent().get(1);
		P p2 = (P) document.getMainDocumentPart().getContent().get(2);
		assertEquals("This paragraph stays untouched.", new ParagraphWrapper(p1).getText());
		assertEquals("This paragraph stays untouched.", new ParagraphWrapper(p2).getText());
	}

	private void paragraphsInTableAreRemoved(WordprocessingMLPackage document) {
		Tbl table = (Tbl) ((JAXBElement<?>) document.getMainDocumentPart().getContent().get(3)).getValue();
		Tr row = (Tr) table.getContent().get(1);

		P p1 = (P) ((Tc) ((JAXBElement<?>) row.getContent().get(0)).getValue()).getContent().get(0);
		P p2 = (P) ((Tc) ((JAXBElement<?>) row.getContent().get(1)).getValue()).getContent().get(0);

		assertEquals("This paragraph stays untouched.", new ParagraphWrapper(p1).getText());
		// since the last paragraph was removed from the cell, an empty paragraph was inserted
		assertEquals("", new ParagraphWrapper(p2).getText());
	}

	private void paragraphsInNestedTablesAreRemoved(WordprocessingMLPackage document) {
		final List<Tbl> tables = DocumentUtil.getTableFromObject(document);

		Tbl nestedTable = tables.get(1);
		Tc cell = (Tc) ((JAXBElement<?>) ((Tr) nestedTable.getContent().get(1)).getContent().get(0)).getValue();
		P p1 = (P) cell.getContent().get(0);

		assertEquals(1, cell.getContent().size());
		assertEquals("This paragraph stays untouched.", new ParagraphWrapper(p1).getText());
	}

	@Test
	public void inlineProcessorExpressionsAreResolved() throws Docx4JException, IOException {
		var context = new Name("Homer");
		var template = getClass().getResourceAsStream("ConditionalDisplayOfParagraphsWithoutCommentTest.docx");
		var stamper = new TestDocxStamper<Name>();
		var document = stamper.stampAndLoad(template, context);
		globalParagraphsAreRemoved(document);
		paragraphsInTableAreRemoved(document);
		paragraphsInNestedTablesAreRemoved(document);
	}

	@Test
	public void unresolvedInlineProcessorExpressionsAreRemoved() throws Docx4JException, IOException {
		var context = new Name("Bart");
		var template = getClass().getResourceAsStream("ConditionalDisplayOfParagraphsWithoutCommentTest.docx");
		var stamper = new TestDocxStamper<Name>();
		var document = stamper.stampAndLoad(template, context);
		globalInlineProcessorExpressionIsRemoved(document);
	}

	private void globalInlineProcessorExpressionIsRemoved(WordprocessingMLPackage document) {
		P p2 = (P) document.getMainDocumentPart().getContent().get(2);
		assertFalse(new ParagraphWrapper(p2).getText().contains("#{"));
	}

	public record Name(String name) {
	}

}
