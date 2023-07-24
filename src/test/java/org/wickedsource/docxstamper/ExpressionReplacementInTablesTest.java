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
import pro.verron.docxstamper.utils.TestDocxStamper;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExpressionReplacementInTablesTest {
	@Test
    void test() throws Docx4JException, IOException {
		var context = new Name("Bart Simpson");
		var template = getClass().getResourceAsStream("ExpressionReplacementInTablesTest.docx");

		var stamper = new TestDocxStamper<Name>(new DocxStamperConfiguration().setFailOnUnresolvedExpression(false));
		var document = stamper.stampAndLoad(template, context);
		resolvedExpressionsAreReplacedInFirstLevelTable(document);
		unresolvedExpressionsAreNotReplacedInFirstLevelTable(document);
		resolvedExpressionsAreReplacedInNestedTable(document);
		unresolvedExpressionsAreNotReplacedInNestedTable(document);
	}

	private void resolvedExpressionsAreReplacedInFirstLevelTable(WordprocessingMLPackage document) {
		Tbl table = (Tbl) ((JAXBElement<?>) document.getMainDocumentPart().getContent().get(1)).getValue();
		Tr row = (Tr) table.getContent().get(0);
		Tc cell = (Tc) ((JAXBElement<?>) row.getContent().get(1)).getValue();
		P nameParagraph = (P) cell.getContent().get(0);
		assertEquals("Bart Simpson", new ParagraphWrapper(nameParagraph).getText());
	}

	private void unresolvedExpressionsAreNotReplacedInFirstLevelTable(WordprocessingMLPackage document) {
		Tbl table = (Tbl) ((JAXBElement<?>) document.getMainDocumentPart().getContent().get(1)).getValue();
		Tr row = (Tr) table.getContent().get(1);
		Tc cell = (Tc) ((JAXBElement<?>) row.getContent().get(1)).getValue();
		P nameParagraph = (P) cell.getContent().get(0);
		assertEquals("${foo}", new ParagraphWrapper(nameParagraph).getText());
	}

	private void resolvedExpressionsAreReplacedInNestedTable(WordprocessingMLPackage document) {
		Tbl table = (Tbl) ((JAXBElement<?>) document.getMainDocumentPart().getContent().get(1)).getValue();
		Tr row = (Tr) table.getContent().get(2);
		Tc cell = (Tc) ((JAXBElement<?>) row.getContent().get(0)).getValue();
		Tbl nestedTable = (Tbl) ((JAXBElement<?>) cell.getContent().get(1)).getValue();
		Tr nestedRow = (Tr) nestedTable.getContent().get(0);
		Tc nestedCell = (Tc) ((JAXBElement<?>) nestedRow.getContent().get(1)).getValue();

		P nameParagraph = (P) nestedCell.getContent().get(0);
		assertEquals("Bart Simpson", new ParagraphWrapper(nameParagraph).getText());
	}

	private void unresolvedExpressionsAreNotReplacedInNestedTable(WordprocessingMLPackage document) {
		Tbl table = (Tbl) ((JAXBElement<?>) document.getMainDocumentPart().getContent().get(1)).getValue();
		Tr row = (Tr) table.getContent().get(2);
		Tc cell = (Tc) ((JAXBElement<?>) row.getContent().get(0)).getValue();
		Tbl nestedTable = (Tbl) ((JAXBElement<?>) cell.getContent().get(1)).getValue();
		Tr nestedRow = (Tr) nestedTable.getContent().get(1);
		Tc nestedCell = (Tc) ((JAXBElement<?>) nestedRow.getContent().get(1)).getValue();

		P nameParagraph = (P) nestedCell.getContent().get(0);
		assertEquals("${foo}", new ParagraphWrapper(nameParagraph).getText());
	}

	public record Name(String name) {
	}
}