package org.wickedsource.docxstamper;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Tr;
import org.junit.Assert;
import org.junit.Test;
import org.wickedsource.docxstamper.context.Character;
import org.wickedsource.docxstamper.context.CharactersContext;
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

        final List<Tbl> tablesFromObject = DocumentUtil.getTableFromObject(document);
        Assert.assertEquals(1, tablesFromObject.size());

        final List<Tr> parentTableRows = DocumentUtil.getTableRowsFromObject(tablesFromObject.get(0));
        // 1 header row + 1 row per character in list
        Assert.assertEquals(7, parentTableRows.size());

        Assert.assertEquals("Homer Simpson", getTextFromCell(parentTableRows, 1, 0));
        Assert.assertEquals("Dan Castellaneta", getTextFromCell(parentTableRows, 1, 1));
        Assert.assertEquals("Marge Simpson", getTextFromCell(parentTableRows, 2, 0));
        Assert.assertEquals("Julie Kavner", getTextFromCell(parentTableRows, 2, 1));
        Assert.assertEquals("Bart Simpson", getTextFromCell(parentTableRows, 3, 0));
        Assert.assertEquals("Nancy Cartwright", getTextFromCell(parentTableRows, 3, 1));
        Assert.assertEquals("Kent Brockman", getTextFromCell(parentTableRows, 4, 0));
        Assert.assertEquals("Harry Shearer", getTextFromCell(parentTableRows, 4, 1));
        Assert.assertEquals("Disco Stu", getTextFromCell(parentTableRows, 5, 0));
        Assert.assertEquals("Hank Azaria", getTextFromCell(parentTableRows, 5, 1));
        Assert.assertEquals("Krusty the Clown", getTextFromCell(parentTableRows, 6, 0));
        Assert.assertEquals("Dan Castellaneta", getTextFromCell(parentTableRows, 6, 1));
    }

    private String getTextFromCell(List<Tr> tableRows, int rowNumber, int cellNumber) {
        return getTextFromCell(DocumentUtil.getTableCellsFromObject(
                tableRows.get(rowNumber).getContent()).get(cellNumber));
    }

    private String getTextFromCell(Tc tc) {
        List<P> paragraphsFromObject = DocumentUtil.getParagraphsFromObject(tc);
        Assert.assertEquals(1, paragraphsFromObject.size());
        return paragraphsFromObject.get(0).toString();
    }
}
