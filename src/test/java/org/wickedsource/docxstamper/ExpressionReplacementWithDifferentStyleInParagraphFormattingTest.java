package org.wickedsource.docxstamper;

import org.docx4j.Docx4J;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.R;
import org.junit.Assert;
import org.junit.Test;
import org.wickedsource.docxstamper.context.StylesContext;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class ExpressionReplacementWithDifferentStyleInParagraphFormattingTest extends AbstractDocx4jTest {

    @Test
    public void test() throws Docx4JException, IOException {

        StylesContext context = new StylesContext();
        context.setStyleItalic("one italic");
        context.setStyleBold("and bold");
        context.setStyleNormal("I am normal");

        InputStream template = getClass().getResourceAsStream("ExpressionReplacementWithDifferentFormattingInParagraphTest.docx");
        WordprocessingMLPackage document = stampAndLoad(template, context);
        Docx4J.save(document, new File("/home/iman/Desktop/testred.docx"));


        // assertItalicStyle((R) ((P) document.getMainDocumentPart().getContent().get(0)).getContent().get(3));
        //  assertsuperscriptStyle((R) ((P) document.getMainDocumentPart().getContent().get(0)).getContent().get(8));
        // assertBoldStyle((R) ((P) document.getMainDocumentPart().getContent().get(0)).getContent().get(8));


    }

    private void assertBoldStyle(R run) {
        Assert.assertTrue("expected Run to be styled bold!", run.getRPr().getB().isVal());
    }

    private void assertItalicStyle(R run) {
        Assert.assertTrue("expected Run to be styled italic!", run.getRPr().getI().isVal());
    }

    private void assertsuperscriptStyle(R run) {
        Assert.assertTrue("expected Run to be styled superscript!", "superscript".equals(run.getRPr().getVertAlign().getVal().value()));
    }


}
