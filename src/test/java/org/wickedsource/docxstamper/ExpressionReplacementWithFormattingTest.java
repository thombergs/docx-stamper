package org.wickedsource.docxstamper;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.junit.Assert;
import org.junit.Test;
import org.wickedsource.docxstamper.context.NameContext;

import java.io.IOException;
import java.io.InputStream;

public class ExpressionReplacementWithFormattingTest extends AbstractDocx4jTest {

    @Test
    public void test() throws Docx4JException, IOException {
        NameContext context = new NameContext();
        context.setName("Homer Simpson");
        InputStream template = getClass().getResourceAsStream("ExpressionReplacementWithFormattingTest.docx");
        WordprocessingMLPackage document = stampAndLoad(template, context);

        assertBoldStyle((R) ((P) document.getMainDocumentPart().getContent().get(2)).getContent().get(1));
        assertItalicStyle((R) ((P) document.getMainDocumentPart().getContent().get(3)).getContent().get(1));
        assertBoldStyle((R) ((P) document.getMainDocumentPart().getContent().get(5)).getContent().get(1));

    }

    private void assertBoldStyle(R run) {
        Assert.assertTrue("expected Run to be styled bold!", run.getRPr().getB().isVal());
    }

    private void assertItalicStyle(R run) {
        Assert.assertTrue("expected Run to be styled italic!", run.getRPr().getI().isVal());
    }


}
