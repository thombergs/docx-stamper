package org.wickedsource.docxstamper.util;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Tr;
import org.junit.Assert;
import org.junit.Test;
import org.wickedsource.docxstamper.AbstractDocx4jTest;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.wickedsource.docxstamper.util.DocumentUtil.getParagraphsFromObject;
import static org.wickedsource.docxstamper.util.DocumentUtil.getTableFromObject;

public class ObjectDeleterTest extends AbstractDocx4jTest {

    @Test
    public void deletesCorrectGlobalParagraphs() throws Docx4JException, IOException {
        InputStream template = getClass().getResourceAsStream("ObjectDeleterTest-globalParagraphs.docx");
        WordprocessingMLPackage document = WordprocessingMLPackage.load(template);

        List<P> coordinates = getParagraphsFromObject(document);

        ObjectDeleter deleter = new ObjectDeleter();
        deleter.deleteParagraph(coordinates.get(0));
        deleter.deleteParagraph(coordinates.get(2));
        deleter.deleteParagraph(coordinates.get(3));

        WordprocessingMLPackage savedDocument = saveAndLoadDocument(document);
        Assert.assertEquals(2, savedDocument.getMainDocumentPart().getContent().size());
        Assert.assertEquals("This is the second paragraph.", new ParagraphWrapper((P) savedDocument.getMainDocumentPart().getContent().get(0)).getText());
        Assert.assertEquals("This is the fifth paragraph.", new ParagraphWrapper((P) savedDocument.getMainDocumentPart().getContent().get(1)).getText());
    }

    @Test
    public void deletesCorrectParagraphsInTableCells() throws Docx4JException, IOException {
        InputStream template = getClass().getResourceAsStream("ObjectDeleterTest-paragraphsInTableCells.docx");
        WordprocessingMLPackage document = WordprocessingMLPackage.load(template);
        final List<P> coordinates = getParagraphsFromObject(getTableFromObject(document));

        ObjectDeleter deleter = new ObjectDeleter();
        deleter.deleteParagraph(coordinates.get(1));
        deleter.deleteParagraph(coordinates.get(2));
        deleter.deleteParagraph(coordinates.get(4));
        deleter.deleteParagraph(coordinates.get(5));
        deleter.deleteParagraph(coordinates.get(8));
        deleter.deleteParagraph(coordinates.get(11));

        WordprocessingMLPackage savedDocument = saveAndLoadDocument(document);
        List<Tc> cellCoordinates = DocumentUtil.getTableCellsFromObject(savedDocument);

        Assert.assertEquals("0 This paragraph stays.", new ParagraphWrapper((P) cellCoordinates.get(0).getContent().get(0)).getText());
        Assert.assertEquals("", new ParagraphWrapper((P) cellCoordinates.get(1).getContent().get(0)).getText());
        Assert.assertEquals("3 This is the second paragraph.", new ParagraphWrapper((P) cellCoordinates.get(2).getContent().get(0)).getText());
        Assert.assertEquals("6 This is the fifth paragraph.", new ParagraphWrapper((P) cellCoordinates.get(2).getContent().get(1)).getText());
        Assert.assertEquals("7 This is the first paragraph.", new ParagraphWrapper((P) cellCoordinates.get(3).getContent().get(0)).getText());
        Assert.assertEquals("9 This is the third paragraph.", new ParagraphWrapper((P) cellCoordinates.get(3).getContent().get(1)).getText());
        Assert.assertEquals("10 This is the fourth paragraph.", new ParagraphWrapper((P) cellCoordinates.get(3).getContent().get(2)).getText());
    }

    @Test
    public void deletesCorrectGlobalTables() throws Docx4JException, IOException {
        InputStream template = getClass().getResourceAsStream("ObjectDeleterTest-tables.docx");
        WordprocessingMLPackage document = WordprocessingMLPackage.load(template);
        final List<Tbl> coordinates = DocumentUtil.getTableFromObject(document);

        ObjectDeleter deleter = new ObjectDeleter();
        deleter.deleteTable(coordinates.get(1));
        deleter.deleteTable(coordinates.get(3));

        WordprocessingMLPackage savedDocument = saveAndLoadDocument(document);

        List<Tbl> newTableCoordinates = DocumentUtil.getTableFromObject(savedDocument);
        Assert.assertEquals(2, newTableCoordinates.size());

        List<Tc> cellCoordinates = DocumentUtil.getTableCellsFromObject(savedDocument);
        Assert.assertEquals("This", new ParagraphWrapper((P) cellCoordinates.get(0).getContent().get(0)).getText());
        Assert.assertEquals("Table", new ParagraphWrapper((P) cellCoordinates.get(1).getContent().get(0)).getText());
        Assert.assertEquals("Stays", new ParagraphWrapper((P) cellCoordinates.get(2).getContent().get(0)).getText());
        Assert.assertEquals("!", new ParagraphWrapper((P) cellCoordinates.get(3).getContent().get(0)).getText());
        Assert.assertEquals("This table stays", new ParagraphWrapper((P) cellCoordinates.get(4).getContent().get(0)).getText());
    }

    @Test
    public void deletesCorrectTableRows() throws Docx4JException, IOException {
        InputStream template = getClass().getResourceAsStream("ObjectDeleterTest-tableRows.docx");
        WordprocessingMLPackage document = WordprocessingMLPackage.load(template);
        final List<Tr> rowCoordinates = DocumentUtil.getTableRowsFromObject(document);

        ObjectDeleter deleter = new ObjectDeleter();
        deleter.deleteTableRow(rowCoordinates.get(2));
        deleter.deleteTableRow(rowCoordinates.get(4));

        WordprocessingMLPackage savedDocument = saveAndLoadDocument(document);

        List<Tr> newRowCoordinates = DocumentUtil.getTableRowsFromObject(savedDocument);
        Assert.assertEquals(3, newRowCoordinates.size());

        List<Tc> cellCoordinates = DocumentUtil.getTableCellsFromObject(savedDocument);
        Assert.assertEquals("This row", new ParagraphWrapper((P) cellCoordinates.get(0).getContent().get(0)).getText());
        Assert.assertEquals("Stays!", new ParagraphWrapper((P) cellCoordinates.get(1).getContent().get(0)).getText());
        Assert.assertEquals("This row", new ParagraphWrapper((P) cellCoordinates.get(2).getContent().get(0)).getText());
        Assert.assertEquals("Stays!", new ParagraphWrapper((P) cellCoordinates.get(3).getContent().get(0)).getText());
        Assert.assertEquals("This row", new ParagraphWrapper((P) cellCoordinates.get(4).getContent().get(0)).getText());
        Assert.assertEquals("Stays!", new ParagraphWrapper((P) cellCoordinates.get(5).getContent().get(0)).getText());
    }
}