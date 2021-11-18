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
import org.wickedsource.docxstamper.util.ParagraphWrapper;
import javax.xml.bind.JAXBElement;
import java.io.IOException;
import java.io.InputStream;

public class ConditionalDisplayOfTablesBug32Test extends AbstractDocx4jTest {

    @Test
    public void test() throws Docx4JException, IOException {
        NameContext context = new NameContext();
        context.setName("Homer");
        InputStream template = getClass().getResourceAsStream("ConditionalDisplayOfTablesBug32Test.docx");
        WordprocessingMLPackage document = stampAndLoad(template, context);
        globalTablesAreRemoved(document);
        nestedTablesAreRemoved(document);
    }

    private void globalTablesAreRemoved(WordprocessingMLPackage document) {
        Assert.assertTrue(document.getMainDocumentPart().getContent().get(1) instanceof P);
        P p1 = (P) document.getMainDocumentPart().getContent().get(1);
        Tbl table2 = (Tbl) ((JAXBElement) document.getMainDocumentPart().getContent().get(3)).getValue();
        Tbl table3 = (Tbl) ((JAXBElement) document.getMainDocumentPart().getContent().get(5)).getValue();
        P p4 = (P) document.getMainDocumentPart().getContent().get(7);

        Assert.assertEquals("This paragraph stays untouched.", new ParagraphWrapper(p1).getText());
        Assert.assertNotNull(table2);
        Assert.assertNotNull(table3);
        Assert.assertEquals("This paragraph stays untouched.", new ParagraphWrapper(p4).getText());
    }

    private void nestedTablesAreRemoved(WordprocessingMLPackage document) {
        Tbl outerTable = (Tbl) ((JAXBElement) document.getMainDocumentPart().getContent().get(3)).getValue();
        Tc cell = (Tc) ((JAXBElement) ((Tr) outerTable.getContent().get(1)).getContent().get(1)).getValue();
        Assert.assertEquals("", new ParagraphWrapper((P) cell.getContent().get(0)).getText()); // empty paragraph, since the last element inside the cell was removed
    }

}