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
import org.wickedsource.docxstamper.replace.ParagraphWrapper;
import org.wickedsource.docxstamper.walk.coordinates.BaseCoordinatesWalker;
import org.wickedsource.docxstamper.walk.coordinates.CoordinatesWalker;
import org.wickedsource.docxstamper.walk.coordinates.TableCoordinates;

import javax.xml.bind.JAXBElement;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ConditionalDisplayOfParagraphsTest extends AbstractDocx4jTest {

    @Test
    public void processorExpressionsInCommentsAreResolved() throws Docx4JException, IOException {
        NameContext context = new NameContext();
        context.setName("Homer");
        InputStream template = getClass().getResourceAsStream("ConditionalDisplayOfParagraphsTest.docx");
        WordprocessingMLPackage document = stampAndLoad(template, context);
        globalParagraphsAreRemoved(document);
        paragraphsInTableAreRemoved(document);
        paragraphsInNestedTablesAreRemoved(document);
    }

    @Test
    public void inlineProcessorExpressionsAreResolved() throws Docx4JException, IOException {
        NameContext context = new NameContext();
        context.setName("Homer");
        InputStream template = getClass().getResourceAsStream("ConditionalDisplayOfParagraphsWithoutCommentTest.docx");
        WordprocessingMLPackage document = stampAndLoad(template, context);
        globalParagraphsAreRemoved(document);
        paragraphsInTableAreRemoved(document);
        paragraphsInNestedTablesAreRemoved(document);
    }

    @Test
    public void unresolvedInlineProcessorExpressionsAreRemoved() throws Docx4JException, IOException {
        NameContext context = new NameContext();
        context.setName("Bart");
        InputStream template = getClass().getResourceAsStream("ConditionalDisplayOfParagraphsWithoutCommentTest.docx");
        WordprocessingMLPackage document = stampAndLoad(template, context);
        globalInlineProcessorExpressionIsRemoved(document);
    }

    private void globalParagraphsAreRemoved(WordprocessingMLPackage document) {
        P p1 = (P) document.getMainDocumentPart().getContent().get(1);
        P p2 = (P) document.getMainDocumentPart().getContent().get(2);
        Assert.assertEquals("This paragraph stays untouched.", new ParagraphWrapper(p1).getText());
        Assert.assertEquals("This paragraph stays untouched.", new ParagraphWrapper(p2).getText());
    }

    private void globalInlineProcessorExpressionIsRemoved(WordprocessingMLPackage document) {
        P p2 = (P) document.getMainDocumentPart().getContent().get(2);
        Assert.assertFalse(new ParagraphWrapper(p2).getText().contains("#{"));
    }

    private void paragraphsInTableAreRemoved(WordprocessingMLPackage document) {
        Tbl table = (Tbl) ((JAXBElement) document.getMainDocumentPart().getContent().get(3)).getValue();
        Tr row = (Tr) table.getContent().get(1);

        P p1 = (P) ((Tc) ((JAXBElement) row.getContent().get(0)).getValue()).getContent().get(0);
        P p2 = (P) ((Tc) ((JAXBElement) row.getContent().get(1)).getValue()).getContent().get(0);

        Assert.assertEquals("This paragraph stays untouched.", new ParagraphWrapper(p1).getText());
        // since the last paragraph was removed from the cell, an empty paragraph was inserted
        Assert.assertEquals("", new ParagraphWrapper(p2).getText());
    }

    private void paragraphsInNestedTablesAreRemoved(WordprocessingMLPackage document) {
        final List<Tbl> tables = new ArrayList<>();
        CoordinatesWalker walker = new BaseCoordinatesWalker(document) {
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
        Assert.assertEquals("This paragraph stays untouched.", new ParagraphWrapper(p1).getText());
    }

}
