package org.wickedsource.docxstamper;

import org.docx4j.TextUtils;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConditionalDisplayOfParagraphsInRepeatTableTest extends AbstractDocx4jTest {

    @Test
    public void test() throws Docx4JException, IOException {
        CharactersContext context = new CharactersContext(new ArrayList<>(Arrays.asList(
                new Character("Homer Simpson", "Dan Castellaneta"),
                new Character("Marge Simpson", "Julie Kavner"),
                new Character("Bart Simpson", "Nancy Cartwright"),
                new Character("Kent Brockman", "Harry Shearer"),
                new Character("Disco Stu", "Hank Azaria"),
                new Character("Krusty the Clown", "Dan Castellaneta"))));

        InputStream template = getClass().getResourceAsStream("ConditionalDisplayOfParagraphsInRepeatTableTest.docx");

        WordprocessingMLPackage document = stampAndLoad(template, context);

        final List<Tbl> tablesFromObject = DocumentUtil.extractElements(document, Tbl.class);
        Assert.assertEquals(1, tablesFromObject.size());

        final List<Tr> parentTableRows = DocumentUtil.extractElements(tablesFromObject.get(0), Tr.class);
        // 1 header row + 1 row per character in list
        Assert.assertEquals(7, parentTableRows.size());

        Assert.assertEquals("This paragraph stays untouched.", getTextFromCell(parentTableRows, 1, 0));
        Assert.assertEquals("", getTextFromCell(parentTableRows, 1, 1));
        Assert.assertEquals("This paragraph stays untouched.", getTextFromCell(parentTableRows, 1, 2));
        Assert.assertEquals("This paragraph stays untouched.", getTextFromCell(parentTableRows, 2, 0));
        Assert.assertEquals("", getTextFromCell(parentTableRows, 2, 1));
        Assert.assertEquals("This paragraph stays untouched.", getTextFromCell(parentTableRows, 2, 2));
        Assert.assertEquals("This paragraph is only included if the name is „Bart“.  ", getTextFromCell(parentTableRows, 3, 0));
        Assert.assertEquals("", getTextFromCell(parentTableRows, 3, 1));
        Assert.assertEquals("This paragraph stays untouched.", getTextFromCell(parentTableRows, 3, 2));
        Assert.assertEquals("This paragraph stays untouched.", getTextFromCell(parentTableRows, 4, 0));
        Assert.assertEquals("", getTextFromCell(parentTableRows, 4, 1));
        Assert.assertEquals("This paragraph stays untouched.", getTextFromCell(parentTableRows, 4, 2));
        Assert.assertEquals("This paragraph stays untouched.", getTextFromCell(parentTableRows, 5, 0));
        Assert.assertEquals("", getTextFromCell(parentTableRows, 5, 1));
        Assert.assertEquals("This paragraph stays untouched.", getTextFromCell(parentTableRows, 5, 2));
        Assert.assertEquals("This paragraph stays untouched.", getTextFromCell(parentTableRows, 6, 0));
        Assert.assertEquals("", getTextFromCell(parentTableRows, 6, 1));
        Assert.assertEquals("This paragraph stays untouched.", getTextFromCell(parentTableRows, 6, 2));
    }

    private String getTextFromCell(List<Tr> tableRows, int rowNumber, int cellNumber) {
        return getTextFromCell(DocumentUtil.extractElements(tableRows.get(rowNumber).getContent(), Tc.class).get(cellNumber));
    }

    private String getTextFromCell(Tc tc) {
        List<P> paragraphsFromObject = DocumentUtil.extractElements(tc, P.class);
        Assert.assertEquals(1, paragraphsFromObject.size());
        return TextUtils.getText(paragraphsFromObject.get(0));
    }
}
