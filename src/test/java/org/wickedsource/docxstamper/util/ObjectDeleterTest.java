package org.wickedsource.docxstamper.util;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.junit.Assert;
import org.junit.Test;
import org.wickedsource.docxstamper.AbstractDocx4jTest;
import org.wickedsource.docxstamper.api.coordinates.ParagraphCoordinates;
import org.wickedsource.docxstamper.api.coordinates.TableCellCoordinates;
import org.wickedsource.docxstamper.api.coordinates.TableCoordinates;
import org.wickedsource.docxstamper.api.coordinates.TableRowCoordinates;
import org.wickedsource.docxstamper.util.ParagraphWrapper;
import org.wickedsource.docxstamper.util.walk.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ObjectDeleterTest extends AbstractDocx4jTest {

    @Test
    public void deletesCorrectGlobalParagraphs() throws Docx4JException, IOException {
        InputStream template = getClass().getResourceAsStream("ObjectDeleterTest-globalParagraphs.docx");
        WordprocessingMLPackage document = WordprocessingMLPackage.load(template);

        int index = 0;
        List<ParagraphCoordinates> coordinates = new ArrayList<>();
        for (Object contentElement : document.getMainDocumentPart().getContent()) {
            P paragraph = (P) contentElement;
            coordinates.add(new ParagraphCoordinates(paragraph, index));
            index++;
        }

        ObjectDeleter deleter = new ObjectDeleter(document);
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
        final List<ParagraphCoordinates> coordinates = getParagraphCoordinates(document);

        ObjectDeleter deleter = new ObjectDeleter(document);
        deleter.deleteParagraph(coordinates.get(1));
        deleter.deleteParagraph(coordinates.get(2));
        deleter.deleteParagraph(coordinates.get(4));
        deleter.deleteParagraph(coordinates.get(5));
        deleter.deleteParagraph(coordinates.get(8));
        deleter.deleteParagraph(coordinates.get(11));

        WordprocessingMLPackage savedDocument = saveAndLoadDocument(document);
        List<TableCellCoordinates> cellCoordinates = getTableCellCoordinats(savedDocument);

        Assert.assertEquals("0 This paragraph stays.", new ParagraphWrapper((P) cellCoordinates.get(0).getCell().getContent().get(0)).getText());
        Assert.assertEquals("", new ParagraphWrapper((P) cellCoordinates.get(1).getCell().getContent().get(0)).getText());
        Assert.assertEquals("3 This is the second paragraph.", new ParagraphWrapper((P) cellCoordinates.get(2).getCell().getContent().get(0)).getText());
        Assert.assertEquals("6 This is the fifth paragraph.", new ParagraphWrapper((P) cellCoordinates.get(2).getCell().getContent().get(1)).getText());
        Assert.assertEquals("7 This is the first paragraph.", new ParagraphWrapper((P) cellCoordinates.get(3).getCell().getContent().get(0)).getText());
        Assert.assertEquals("9 This is the third paragraph.", new ParagraphWrapper((P) cellCoordinates.get(3).getCell().getContent().get(1)).getText());
        Assert.assertEquals("10 This is the fourth paragraph.", new ParagraphWrapper((P) cellCoordinates.get(3).getCell().getContent().get(2)).getText());
    }

    @Test
    public void deletesCorrectGlobalTables() throws Docx4JException, IOException {
        InputStream template = getClass().getResourceAsStream("ObjectDeleterTest-tables.docx");
        WordprocessingMLPackage document = WordprocessingMLPackage.load(template);
        final List<TableCoordinates> coordinates = getTableCoordinats(document);

        ObjectDeleter deleter = new ObjectDeleter(document);
        deleter.deleteTable(coordinates.get(1));
        deleter.deleteTable(coordinates.get(3));

        WordprocessingMLPackage savedDocument = saveAndLoadDocument(document);

        List<TableCoordinates> newTableCoordinates = getTableCoordinats(savedDocument);
        Assert.assertEquals(2, newTableCoordinates.size());

        List<TableCellCoordinates> cellCoordinates = getTableCellCoordinats(savedDocument);
        Assert.assertEquals("This", new ParagraphWrapper((P) cellCoordinates.get(0).getCell().getContent().get(0)).getText());
        Assert.assertEquals("Table", new ParagraphWrapper((P) cellCoordinates.get(1).getCell().getContent().get(0)).getText());
        Assert.assertEquals("Stays", new ParagraphWrapper((P) cellCoordinates.get(2).getCell().getContent().get(0)).getText());
        Assert.assertEquals("!", new ParagraphWrapper((P) cellCoordinates.get(3).getCell().getContent().get(0)).getText());
        Assert.assertEquals("This table stays", new ParagraphWrapper((P) cellCoordinates.get(4).getCell().getContent().get(0)).getText());
    }

    @Test
    public void deletesCorrectTableRows() throws Docx4JException, IOException {
        InputStream template = getClass().getResourceAsStream("ObjectDeleterTest-tableRows.docx");
        WordprocessingMLPackage document = WordprocessingMLPackage.load(template);
        final List<TableRowCoordinates> rowCoordinates = getTableRowCoordinats(document);

        ObjectDeleter deleter = new ObjectDeleter(document);
        deleter.deleteTableRow(rowCoordinates.get(2));
        deleter.deleteTableRow(rowCoordinates.get(4));

        WordprocessingMLPackage savedDocument = saveAndLoadDocument(document);

        List<TableRowCoordinates> newRowCoordinates = getTableRowCoordinats(savedDocument);
        Assert.assertEquals(3, newRowCoordinates.size());

        List<TableCellCoordinates> cellCoordinates = getTableCellCoordinats(savedDocument);
        Assert.assertEquals("This row", new ParagraphWrapper((P) cellCoordinates.get(0).getCell().getContent().get(0)).getText());
        Assert.assertEquals("Stays!", new ParagraphWrapper((P) cellCoordinates.get(1).getCell().getContent().get(0)).getText());
        Assert.assertEquals("This row", new ParagraphWrapper((P) cellCoordinates.get(2).getCell().getContent().get(0)).getText());
        Assert.assertEquals("Stays!", new ParagraphWrapper((P) cellCoordinates.get(3).getCell().getContent().get(0)).getText());
        Assert.assertEquals("This row", new ParagraphWrapper((P) cellCoordinates.get(4).getCell().getContent().get(0)).getText());
        Assert.assertEquals("Stays!", new ParagraphWrapper((P) cellCoordinates.get(5).getCell().getContent().get(0)).getText());
    }

    private List<ParagraphCoordinates> getParagraphCoordinates(WordprocessingMLPackage document) {
        final List<ParagraphCoordinates> resultList = new ArrayList<>();
        CoordinatesWalker walker = new BaseCoordinatesWalker(document) {
            @Override
            protected void onParagraph(ParagraphCoordinates paragraphCoordinates) {
                resultList.add(paragraphCoordinates);
            }
        };
        walker.walk();
        return resultList;
    }

    private List<TableCellCoordinates> getTableCellCoordinats(WordprocessingMLPackage document) {
        final List<TableCellCoordinates> resultList = new ArrayList<>();
        CoordinatesWalker walker = new BaseCoordinatesWalker(document) {
            @Override
            protected void onTableCell(TableCellCoordinates tableCellCoordinates) {
                resultList.add(tableCellCoordinates);
            }
        };
        walker.walk();
        return resultList;
    }

    private List<TableCoordinates> getTableCoordinats(WordprocessingMLPackage document) {
        final List<TableCoordinates> resultList = new ArrayList<>();
        CoordinatesWalker walker = new BaseCoordinatesWalker(document) {
            @Override
            protected void onTable(TableCoordinates tableCoordinates) {
                resultList.add(tableCoordinates);
            }
        };
        walker.walk();
        return resultList;
    }

    private List<TableRowCoordinates> getTableRowCoordinats(WordprocessingMLPackage document) {
        final List<TableRowCoordinates> resultList = new ArrayList<>();
        CoordinatesWalker walker = new BaseCoordinatesWalker(document) {
            @Override
            protected void onTableRow(TableRowCoordinates tableRowCoordinates) {
                resultList.add(tableRowCoordinates);
            }
        };
        walker.walk();
        return resultList;
    }

}