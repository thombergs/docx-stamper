package org.wickedsource.docxstamper;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Tr;
import org.junit.Assert;
import org.junit.Test;
import org.wickedsource.docxstamper.docx4j.AbstractDocx4jTest;
import org.wickedsource.docxstamper.docx4j.RunAggregator;
import org.wickedsource.docxstamper.docx4j.TestContext;

import javax.xml.bind.JAXBElement;
import java.io.IOException;
import java.io.InputStream;

public class ExpressionReplacementInTablesTest extends AbstractDocx4jTest {

    @Test
    public void test() throws Docx4JException, IOException {
        TestContext context = new TestContext();
        context.setName("Bart Simpson");
        InputStream template = getClass().getResourceAsStream("ExpressionReplacementInTablesTest.docx");
        WordprocessingMLPackage document = stampAndLoad(template, context);
        resolvedExpressionsAreReplacedInFirstLevelTable(document);
        unresolvedExpressionsAreNotReplacedInFirstLevelTable(document);
        resolvedExpressionsAreReplacedInNestedTable(document);
        unresolvedExpressionsAreNotReplacedInNestedTable(document);
    }

    private void resolvedExpressionsAreReplacedInFirstLevelTable(WordprocessingMLPackage document) {
        Tbl table = (Tbl) ((JAXBElement) document.getMainDocumentPart().getContent().get(1)).getValue();
        Tr row = (Tr) table.getContent().get(0);
        Tc cell = (Tc) ((JAXBElement) row.getContent().get(1)).getValue();
        P nameParagraph = (P) cell.getContent().get(0);
        Assert.assertEquals("Bart Simpson", new RunAggregator(nameParagraph).getText());
    }

    private void unresolvedExpressionsAreNotReplacedInFirstLevelTable(WordprocessingMLPackage document) {
        Tbl table = (Tbl) ((JAXBElement) document.getMainDocumentPart().getContent().get(1)).getValue();
        Tr row = (Tr) table.getContent().get(1);
        Tc cell = (Tc) ((JAXBElement) row.getContent().get(1)).getValue();
        P nameParagraph = (P) cell.getContent().get(0);
        Assert.assertEquals("${foo}", new RunAggregator(nameParagraph).getText());
    }

    private void resolvedExpressionsAreReplacedInNestedTable(WordprocessingMLPackage document) {
        Tbl table = (Tbl) ((JAXBElement) document.getMainDocumentPart().getContent().get(1)).getValue();
        Tr row = (Tr) table.getContent().get(2);
        Tc cell = (Tc) ((JAXBElement) row.getContent().get(0)).getValue();
        Tbl nestedTable = (Tbl) ((JAXBElement) cell.getContent().get(1)).getValue();
        Tr nestedRow = (Tr) nestedTable.getContent().get(0);
        Tc nestedCell = (Tc) ((JAXBElement) nestedRow.getContent().get(1)).getValue();

        P nameParagraph = (P) nestedCell.getContent().get(0);
        Assert.assertEquals("Bart Simpson", new RunAggregator(nameParagraph).getText());
    }

    private void unresolvedExpressionsAreNotReplacedInNestedTable(WordprocessingMLPackage document) {
        Tbl table = (Tbl) ((JAXBElement) document.getMainDocumentPart().getContent().get(1)).getValue();
        Tr row = (Tr) table.getContent().get(2);
        Tc cell = (Tc) ((JAXBElement) row.getContent().get(0)).getValue();
        Tbl nestedTable = (Tbl) ((JAXBElement) cell.getContent().get(1)).getValue();
        Tr nestedRow = (Tr) nestedTable.getContent().get(1);
        Tc nestedCell = (Tc) ((JAXBElement) nestedRow.getContent().get(1)).getValue();

        P nameParagraph = (P) nestedCell.getContent().get(0);
        Assert.assertEquals("${foo}", new RunAggregator(nameParagraph).getText());
    }


}
