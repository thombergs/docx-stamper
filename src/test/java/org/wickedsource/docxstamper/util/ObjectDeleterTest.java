package org.wickedsource.docxstamper.util;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Tr;
import org.junit.jupiter.api.Test;
import pro.verron.docxstamper.utils.IOStreams;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.wickedsource.docxstamper.util.DocumentUtil.getParagraphsFromObject;
import static org.wickedsource.docxstamper.util.DocumentUtil.getTableFromObject;

class ObjectDeleterTest {

    @Test
    void deletesCorrectGlobalParagraphs() throws Docx4JException, IOException {
        var template = getClass().getResourceAsStream("ObjectDeleterTest-globalParagraphs.docx");
        var in = WordprocessingMLPackage.load(template);
        var coordinates = getParagraphsFromObject(in);

        ObjectDeleter.deleteParagraph(coordinates.get(0));
        ObjectDeleter.deleteParagraph(coordinates.get(2));
        ObjectDeleter.deleteParagraph(coordinates.get(3));

        var document = saveAndLoadDocument(in);
        assertEquals(2, document.getMainDocumentPart().getContent().size());
        assertEquals("This is the second paragraph.",
                new ParagraphWrapper((P) document.getMainDocumentPart()
                        .getContent()
                        .get(0)).getText());
        assertEquals("This is the fifth paragraph.",
                new ParagraphWrapper((P) document.getMainDocumentPart()
                        .getContent()
                        .get(1)).getText());
    }

    /**
     * Saves the given document into a temporal ByteArrayOutputStream and loads it from there again. This is useful to
     * check if changes in the Docx4j object structure are really transported into the XML of the .docx file.
     *
     * @param document the document to save and load again.
     * @return the document after it has been saved and loaded again.
     */
    public WordprocessingMLPackage saveAndLoadDocument(WordprocessingMLPackage document) throws Docx4JException, IOException {
        var out = IOStreams.getOutputStream();
        document.save(out);
        var in = IOStreams.getInputStream(out);
        return WordprocessingMLPackage.load(in);
    }

    @Test
    void deletesCorrectParagraphsInTableCells() throws Docx4JException, IOException {
        InputStream template = getClass().getResourceAsStream("ObjectDeleterTest-paragraphsInTableCells.docx");
        WordprocessingMLPackage document = WordprocessingMLPackage.load(template);
        final List<P> coordinates = getParagraphsFromObject(getTableFromObject(document));

        ObjectDeleter.deleteParagraph(coordinates.get(1));
        ObjectDeleter.deleteParagraph(coordinates.get(2));
        ObjectDeleter.deleteParagraph(coordinates.get(4));
        ObjectDeleter.deleteParagraph(coordinates.get(5));
        ObjectDeleter.deleteParagraph(coordinates.get(8));
        ObjectDeleter.deleteParagraph(coordinates.get(11));

        WordprocessingMLPackage savedDocument = saveAndLoadDocument(document);
        List<Tc> cellCoordinates = DocumentUtil.getTableCellsFromObject(savedDocument);

        assertEquals("0 This paragraph stays.",
                new ParagraphWrapper((P) cellCoordinates.get(0).getContent().get(0)).getText());
        assertEquals("", new ParagraphWrapper((P) cellCoordinates.get(1).getContent().get(0)).getText());
        assertEquals("3 This is the second paragraph.",
                new ParagraphWrapper((P) cellCoordinates.get(2).getContent().get(0)).getText());
        assertEquals("6 This is the fifth paragraph.",
                new ParagraphWrapper((P) cellCoordinates.get(2).getContent().get(1)).getText());
        assertEquals("7 This is the first paragraph.",
                new ParagraphWrapper((P) cellCoordinates.get(3).getContent().get(0)).getText());
        assertEquals("9 This is the third paragraph.",
                new ParagraphWrapper((P) cellCoordinates.get(3).getContent().get(1)).getText());
        assertEquals("10 This is the fourth paragraph.",
                new ParagraphWrapper((P) cellCoordinates.get(3).getContent().get(2)).getText());
    }

    @Test
    void deletesCorrectGlobalTables() throws Docx4JException, IOException {
        InputStream template = getClass().getResourceAsStream("ObjectDeleterTest-tables.docx");
        WordprocessingMLPackage document = WordprocessingMLPackage.load(template);
        final List<Tbl> coordinates = DocumentUtil.getTableFromObject(document);

        ObjectDeleter.deleteTable(coordinates.get(1));
        ObjectDeleter.deleteTable(coordinates.get(3));

        WordprocessingMLPackage savedDocument = saveAndLoadDocument(document);

        List<Tbl> newTableCoordinates = DocumentUtil.getTableFromObject(savedDocument);
        assertEquals(2, newTableCoordinates.size());

        List<Tc> cellCoordinates = DocumentUtil.getTableCellsFromObject(savedDocument);
        assertEquals("This", new ParagraphWrapper((P) cellCoordinates.get(0).getContent().get(0)).getText());
        assertEquals("Table", new ParagraphWrapper((P) cellCoordinates.get(1).getContent().get(0)).getText());
        assertEquals("Stays", new ParagraphWrapper((P) cellCoordinates.get(2).getContent().get(0)).getText());
        assertEquals("!", new ParagraphWrapper((P) cellCoordinates.get(3).getContent().get(0)).getText());
        assertEquals("This table stays", new ParagraphWrapper((P) cellCoordinates.get(4).getContent().get(0)).getText());
    }

    @Test
    void deletesCorrectTableRows() throws Docx4JException, IOException {
        InputStream template = getClass().getResourceAsStream("ObjectDeleterTest-tableRows.docx");
        WordprocessingMLPackage document = WordprocessingMLPackage.load(template);
        final List<Tr> rowCoordinates = DocumentUtil.getTableRowsFromObject(document);

        ObjectDeleter.deleteTableRow(rowCoordinates.get(2));
        ObjectDeleter.deleteTableRow(rowCoordinates.get(4));

        WordprocessingMLPackage savedDocument = saveAndLoadDocument(document);

        List<Tr> newRowCoordinates = DocumentUtil.getTableRowsFromObject(savedDocument);
        assertEquals(3, newRowCoordinates.size());

        List<Tc> cellCoordinates = DocumentUtil.getTableCellsFromObject(savedDocument);
        assertEquals("This row", new ParagraphWrapper((P) cellCoordinates.get(0).getContent().get(0)).getText());
        assertEquals("Stays!", new ParagraphWrapper((P) cellCoordinates.get(1).getContent().get(0)).getText());
        assertEquals("This row", new ParagraphWrapper((P) cellCoordinates.get(2).getContent().get(0)).getText());
        assertEquals("Stays!", new ParagraphWrapper((P) cellCoordinates.get(3).getContent().get(0)).getText());
        assertEquals("This row", new ParagraphWrapper((P) cellCoordinates.get(4).getContent().get(0)).getText());
        assertEquals("Stays!", new ParagraphWrapper((P) cellCoordinates.get(5).getContent().get(0)).getText());
    }
}