package org.wickedsource.docxstamper;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Tr;
import org.junit.Assert;
import org.junit.Test;
import org.wickedsource.docxstamper.context.NameContext;
import org.wickedsource.docxstamper.util.DocumentUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class ConditionalDisplayOfTableRowsTest extends AbstractDocx4jTest {

    @Test
    public void test() throws Docx4JException, IOException {
        NameContext context = new NameContext();
        context.setName("Homer");
        InputStream template = getClass().getResourceAsStream("ConditionalDisplayOfTableRowsTest.docx");
        WordprocessingMLPackage document = stampAndLoad(template, context);

        final List<Tbl> tablesFromObject = DocumentUtil.getTableFromObject(document);
        Assert.assertEquals(2, tablesFromObject.size());

        final List<Tr> parentTableRows = DocumentUtil.getTableRowsFromObject(tablesFromObject.get(0));
        // gets all the rows within the table and the nested table
        Assert.assertEquals(5, parentTableRows.size());

        final List<Tr> nestedTableRows = DocumentUtil.getTableRowsFromObject(tablesFromObject.get(1));
        Assert.assertEquals(2, nestedTableRows.size());

        final List<Tc> parentTableCells = DocumentUtil.getTableCellsFromObject(tablesFromObject.get(0));
        // gets all the cells within the table and the nested table
        Assert.assertEquals(5, parentTableCells.size());

        Assert.assertEquals("This row stays untouched.", getTextFromCell(parentTableCells.get(0)));
        Assert.assertEquals("This row stays untouched.", getTextFromCell(parentTableCells.get(1)));
        Assert.assertEquals("Also works on nested Tables", getTextFromCell(parentTableCells.get(3)));
        Assert.assertEquals("This row stays untouched.", getTextFromCell(parentTableCells.get(4)));
    }

    private String getTextFromCell(Tc tc) {
        List<P> paragraphsFromObject = DocumentUtil.getParagraphsFromObject(tc);
        Assert.assertEquals(1, paragraphsFromObject.size());
        return paragraphsFromObject.get(0).toString();
    }
}
