package org.wickedsource.docxstamper;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.junit.Assert;
import org.junit.Test;
import org.wickedsource.docxstamper.replace.ParagraphWrapper;

import java.io.IOException;
import java.io.InputStream;

public class ExpressionWithSurroundingSpacesTest extends AbstractDocx4jTest {

    @Test
    public void test() throws Docx4JException, IOException {
        Context context = new Context();
        InputStream template = getClass().getResourceAsStream("ExpressionWithSurroundingSpacesTest.docx");
        WordprocessingMLPackage document = stampAndLoad(template, context);

        Assert.assertEquals("Before Expression After.", new ParagraphWrapper((P) document.getMainDocumentPart().getContent().get(2)).getText());
        Assert.assertEquals("Before Expression After.", new ParagraphWrapper((P) document.getMainDocumentPart().getContent().get(3)).getText());
        Assert.assertEquals("Before Expression After.", new ParagraphWrapper((P) document.getMainDocumentPart().getContent().get(4)).getText());
        Assert.assertEquals("Before Expression After.", new ParagraphWrapper((P) document.getMainDocumentPart().getContent().get(5)).getText());
        Assert.assertEquals("Before Expression After.", new ParagraphWrapper((P) document.getMainDocumentPart().getContent().get(6)).getText());
        Assert.assertEquals("Before Expression After.", new ParagraphWrapper((P) document.getMainDocumentPart().getContent().get(7)).getText());
        Assert.assertEquals("Before Expression After.", new ParagraphWrapper((P) document.getMainDocumentPart().getContent().get(8)).getText());
    }

    static class Context {
        private String expressionWithLeadingAndTrailingSpace = " Expression ";
        private String expressionWithLeadingSpace = " Expression";
        private String expressionWithTrailingSpace = "Expression ";
        private String expressionWithoutSpaces = "Expression";

        public String getExpressionWithLeadingAndTrailingSpace() {
            return expressionWithLeadingAndTrailingSpace;
        }

        public String getExpressionWithLeadingSpace() {
            return expressionWithLeadingSpace;
        }

        public String getExpressionWithTrailingSpace() {
            return expressionWithTrailingSpace;
        }

        public String getExpressionWithoutSpaces() {
            return expressionWithoutSpaces;
        }
    }


}
