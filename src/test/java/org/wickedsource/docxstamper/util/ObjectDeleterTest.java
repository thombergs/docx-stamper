package org.wickedsource.docxstamper.util;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tr;
import org.junit.Assert;
import org.junit.Test;
import org.wickedsource.docxstamper.AbstractDocx4jTest;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class ObjectDeleterTest extends AbstractDocx4jTest {

    @Test
    public void deletesCorrectGlobalParagraphs() throws Docx4JException, IOException {
        InputStream template = getClass().getResourceAsStream("ObjectDeleterTest-globalParagraphs.docx");
        WordprocessingMLPackage document = WordprocessingMLPackage.load(template);

        List<P> paragraphsFromDocument = DocumentUtil.extractElements(document, P.class);

        ObjectDeleter deleter = new ObjectDeleter();
        deleter.deleteParagraph(paragraphsFromDocument.get(0));
        deleter.deleteParagraph(paragraphsFromDocument.get(2));
        deleter.deleteParagraph(paragraphsFromDocument.get(3));

        WordprocessingMLPackage savedDocument = saveAndLoadDocument(document);
        Assert.assertEquals(2, savedDocument.getMainDocumentPart().getContent().size());
        Assert.assertEquals("This is the second paragraph.", new ParagraphWrapper((P) savedDocument.getMainDocumentPart().getContent().get(0)).getText());
        Assert.assertEquals("This is the fifth paragraph.", new ParagraphWrapper((P) savedDocument.getMainDocumentPart().getContent().get(1)).getText());
    }

    @Test
    public void deletesCorrectParagraphsInTableCells() throws Docx4JException, IOException {
        InputStream template = getClass().getResourceAsStream("ObjectDeleterTest-paragraphsInTableCells.docx");
        WordprocessingMLPackage document = WordprocessingMLPackage.load(template);

        List<P> paragraphsFromTable = DocumentUtil.extractElements(DocumentUtil.extractElements(document, Tbl.class), P.class);
        Assert.assertEquals(12, paragraphsFromTable.size());

        ObjectDeleter deleter = new ObjectDeleter();
        deleter.deleteParagraph(paragraphsFromTable.get(1));
        deleter.deleteParagraph(paragraphsFromTable.get(2));
        deleter.deleteParagraph(paragraphsFromTable.get(4));
        deleter.deleteParagraph(paragraphsFromTable.get(5));
        deleter.deleteParagraph(paragraphsFromTable.get(8));
        deleter.deleteParagraph(paragraphsFromTable.get(11));

        WordprocessingMLPackage savedDocument = saveAndLoadDocument(document);

        List<P> paragraphsFromUpdatedTable = DocumentUtil.extractElements(DocumentUtil.extractElements(savedDocument, Tbl.class), P.class);

        Assert.assertEquals("0 This paragraph stays.", new ParagraphWrapper(paragraphsFromUpdatedTable.get(0)).getText());
        Assert.assertEquals("", new ParagraphWrapper(paragraphsFromUpdatedTable.get(1)).getText());
        Assert.assertEquals("3 This is the second paragraph.", new ParagraphWrapper(paragraphsFromUpdatedTable.get(2)).getText());
        Assert.assertEquals("6 This is the fifth paragraph.", new ParagraphWrapper(paragraphsFromUpdatedTable.get(3)).getText());
        Assert.assertEquals("7 This is the first paragraph.", new ParagraphWrapper(paragraphsFromUpdatedTable.get(4)).getText());
        Assert.assertEquals("9 This is the third paragraph.", new ParagraphWrapper(paragraphsFromUpdatedTable.get(5)).getText());
        Assert.assertEquals("10 This is the fourth paragraph.", new ParagraphWrapper(paragraphsFromUpdatedTable.get(6)).getText());
    }

    @Test
    public void deletesCorrectGlobalTables() throws Docx4JException, IOException {
        InputStream template = getClass().getResourceAsStream("ObjectDeleterTest-tables.docx");
        WordprocessingMLPackage document = WordprocessingMLPackage.load(template);
        List<Tbl> tablesFromDocument = DocumentUtil.extractElements(document, Tbl.class);

        ObjectDeleter deleter = new ObjectDeleter();
        deleter.deleteTable(tablesFromDocument.get(1));
        deleter.deleteTable(tablesFromDocument.get(3));

        WordprocessingMLPackage savedDocument = saveAndLoadDocument(document);

        List<Tbl> tablesFromUpdatedDocument = DocumentUtil.extractElements(savedDocument, Tbl.class);

        Assert.assertEquals(2, tablesFromUpdatedDocument.size());

        List<P> paragraphsFromFirstTable = DocumentUtil.extractElements(tablesFromUpdatedDocument.get(0), P.class);

        Assert.assertEquals("This", new ParagraphWrapper(paragraphsFromFirstTable.get(0)).getText());
        Assert.assertEquals("Table", new ParagraphWrapper(paragraphsFromFirstTable.get(1)).getText());
        Assert.assertEquals("Stays", new ParagraphWrapper(paragraphsFromFirstTable.get(2)).getText());
        Assert.assertEquals("!", new ParagraphWrapper(paragraphsFromFirstTable.get(3)).getText());

        List<P> paragraphsFromSecondTable = DocumentUtil.extractElements(tablesFromUpdatedDocument.get(1), P.class);

        Assert.assertEquals("This table stays", new ParagraphWrapper(paragraphsFromSecondTable.get(0)).getText());
    }

    @Test
    public void deletesCorrectTableRows() throws Docx4JException, IOException {
        InputStream template = getClass().getResourceAsStream("ObjectDeleterTest-tableRows.docx");
        WordprocessingMLPackage document = WordprocessingMLPackage.load(template);

        List<Tr> tableRowsFromDocument = DocumentUtil.extractElements(DocumentUtil.extractElements(document, Tbl.class).get(0), Tr.class);

        ObjectDeleter deleter = new ObjectDeleter();
        deleter.deleteTableRow(tableRowsFromDocument.get(2));
        deleter.deleteTableRow(tableRowsFromDocument.get(4));

        WordprocessingMLPackage savedDocument = saveAndLoadDocument(document);

        List<Tr> tableRowsFromUpdatedDocument = DocumentUtil.extractElements(DocumentUtil.extractElements(savedDocument, Tbl.class).get(0), Tr.class);

        Assert.assertEquals(3, tableRowsFromUpdatedDocument.size());
        List<P> paragraphsFromObject = DocumentUtil.extractElements(tableRowsFromUpdatedDocument, P.class);

        Assert.assertEquals("This row", new ParagraphWrapper(paragraphsFromObject.get(0)).getText());
        Assert.assertEquals("Stays!", new ParagraphWrapper(paragraphsFromObject.get(1)).getText());
        Assert.assertEquals("This row", new ParagraphWrapper(paragraphsFromObject.get(2)).getText());
        Assert.assertEquals("Stays!", new ParagraphWrapper(paragraphsFromObject.get(3)).getText());
        Assert.assertEquals("This row", new ParagraphWrapper(paragraphsFromObject.get(4)).getText());
        Assert.assertEquals("Stays!", new ParagraphWrapper(paragraphsFromObject.get(5)).getText());
    }

}