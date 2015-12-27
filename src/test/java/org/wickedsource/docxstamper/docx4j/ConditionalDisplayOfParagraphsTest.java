package org.wickedsource.docxstamper.docx4j;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Tr;
import org.junit.Assert;
import org.junit.Test;
import org.wickedsource.docxstamper.docx4j.walk.TableWalker;
import org.wickedsource.docxstamper.docx4j.walk.coordinates.TableCoordinates;

import javax.xml.bind.JAXBElement;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ConditionalDisplayOfParagraphsTest extends AbstractDocx4jTest {

    @Test
    public void test() throws Docx4JException, IOException {
        TestContext context = new TestContext();
        context.setName("Homer");
        InputStream template = getClass().getResourceAsStream("ConditionalDisplayOfParagraphsTest.docx");
        WordprocessingMLPackage document = stampAndLoad(template, context);
        globalParagraphsAreRemoved(document);
        paragraphsInTableAreRemoved(document);
        paragraphsInNestedTablesAreRemoved(document);
    }

    private void globalParagraphsAreRemoved(WordprocessingMLPackage document) {
        P p1 = (P) document.getMainDocumentPart().getContent().get(1);
        P p2 = (P) document.getMainDocumentPart().getContent().get(2);
        Assert.assertEquals("This paragraph stays untouched.", new RunAggregator(p1).getText());
        Assert.assertEquals("This paragraph stays untouched.", new RunAggregator(p2).getText());
    }

    private void paragraphsInTableAreRemoved(WordprocessingMLPackage document) {
        Tbl table = (Tbl) ((JAXBElement) document.getMainDocumentPart().getContent().get(3)).getValue();
        Tr row = (Tr) table.getContent().get(1);

        P p1 = (P) ((Tc) ((JAXBElement) row.getContent().get(0)).getValue()).getContent().get(0);
        P p2 = (P) ((Tc) ((JAXBElement) row.getContent().get(1)).getValue()).getContent().get(0);

        Assert.assertEquals("This paragraph stays untouched.", new RunAggregator(p1).getText());
        // since the last paragraph was removed from the cell, an empty paragraph was inserted
        Assert.assertEquals("", new RunAggregator(p2).getText());
    }

    private void paragraphsInNestedTablesAreRemoved(WordprocessingMLPackage document) {
        final List<Tbl> tables = new ArrayList<>();
        TableWalker walker = new TableWalker(document) {
            @Override
            protected void onTable(TableCoordinates tableCoordinates) {
                tables.add(tableCoordinates.getTable());
            }
        };
        walker.walk();

        Tbl nestedTable = tables.get(1);
        Tc cell = (Tc) ((JAXBElement) ((Tr) nestedTable.getContent().get(1)).getContent().get(0)).getValue();
        P p1 = (P) cell.getContent().get(0);

        Assert.assertEquals(1, cell.getContent().size());
        Assert.assertEquals("This paragraph stays untouched.", new RunAggregator(p1).getText());
    }

}
