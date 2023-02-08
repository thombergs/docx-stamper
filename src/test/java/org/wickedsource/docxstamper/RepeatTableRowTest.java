package org.wickedsource.docxstamper;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Tr;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Tr;
import org.junit.Assert;
import org.junit.Test;
import org.wickedsource.docxstamper.context.Character;
import org.wickedsource.docxstamper.context.CharactersContext;
import org.wickedsource.docxstamper.util.DocumentUtil;
import org.wickedsource.docxstamper.util.ParagraphWrapper;
import org.wickedsource.docxstamper.util.DocumentUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class RepeatTableRowTest extends AbstractDocx4jTest {

    @Test
    public void test() throws Docx4JException, IOException {
        CharactersContext context = new CharactersContext();
        context.getCharacters().add(new Character("Homer Simpson", "Dan Castellaneta"));
        context.getCharacters().add(new Character("Marge Simpson", "Julie Kavner"));
        context.getCharacters().add(new Character("Bart Simpson", "Nancy Cartwright"));
        context.getCharacters().add(new Character("Kent Brockman", "Harry Shearer"));
        context.getCharacters().add(new Character("Disco Stu", "Hank Azaria"));
        context.getCharacters().add(new Character("Krusty the Clown", "Dan Castellaneta"));
        InputStream template = getClass().getResourceAsStream("RepeatTableRowTest.docx");

        WordprocessingMLPackage document = stampAndLoad(template, context);

        final List<Tr> rows = DocumentUtil.getTableRowsFromObject(document);
        final List<Tbl> tablesFromObject = DocumentUtil.extractElements(document, Tbl.class);
        Assert.assertEquals(1, tablesFromObject.size());

        final List<Tr> parentTableRows = DocumentUtil.extractElements(tablesFromObject.get(0), Tr.class);
        // 1 header row + 1 row per character in list
        Assert.assertEquals(7, rows.size());

        Assert.assertEquals(7, parentTableRows.size());

        final List<Tc> cells = DocumentUtil.getTableCellsFromObject(document);

        Assert.assertEquals("Homer Simpson", new ParagraphWrapper((P) cells.get(2).getContent().get(0)).getText());
        Assert.assertEquals("Dan Castellaneta", new ParagraphWrapper((P) cells.get(3).getContent().get(0)).getText());
        Assert.assertEquals("Marge Simpson", new ParagraphWrapper((P) cells.get(4).getContent().get(0)).getText());
        Assert.assertEquals("Julie Kavner", new ParagraphWrapper((P) cells.get(5).getContent().get(0)).getText());
        Assert.assertEquals("Bart Simpson", new ParagraphWrapper((P) cells.get(6).getContent().get(0)).getText());
        Assert.assertEquals("Nancy Cartwright", new ParagraphWrapper((P) cells.get(7).getContent().get(0)).getText());
        Assert.assertEquals("Kent Brockman", new ParagraphWrapper((P) cells.get(8).getContent().get(0)).getText());
        Assert.assertEquals("Harry Shearer", new ParagraphWrapper((P) cells.get(9).getContent().get(0)).getText());
        Assert.assertEquals("Disco Stu", new ParagraphWrapper((P) cells.get(10).getContent().get(0)).getText());
        Assert.assertEquals("Hank Azaria", new ParagraphWrapper((P) cells.get(11).getContent().get(0)).getText());
        Assert.assertEquals("Krusty the Clown", new ParagraphWrapper((P) cells.get(12).getContent().get(0)).getText());
        Assert.assertEquals("Dan Castellaneta", new ParagraphWrapper((P) cells.get(13).getContent().get(0)).getText());
    }

    private String getTextFromCell(List<Tr> tableRows, int rowNumber, int cellNumber) {
        return getTextFromCell(DocumentUtil.extractElements(tableRows.get(rowNumber).getContent(), Tc.class).get(cellNumber));
    }

    private String getTextFromCell(Tc tc) {
        List<P> paragraphsFromObject = DocumentUtil.extractElements(tc, P.class);
        Assert.assertEquals(1, paragraphsFromObject.size());
        return paragraphsFromObject.get(0).toString();
    }
}
