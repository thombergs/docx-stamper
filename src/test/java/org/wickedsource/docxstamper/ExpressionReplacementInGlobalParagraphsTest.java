package org.wickedsource.docxstamper;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.junit.Assert;
import org.junit.Test;
import org.wickedsource.docxstamper.context.NameContext;
import org.wickedsource.docxstamper.docx4j.AbstractDocx4jTest;
import org.wickedsource.docxstamper.docx4j.util.ParagraphWrapper;

import java.io.IOException;
import java.io.InputStream;

public class ExpressionReplacementInGlobalParagraphsTest extends AbstractDocx4jTest {

    @Test
    public void test() throws Docx4JException, IOException {
        NameContext context = new NameContext();
        context.setName("Homer Simpson");
        InputStream template = getClass().getResourceAsStream("ExpressionReplacementInGlobalParagraphsTest.docx");
        WordprocessingMLPackage document = stampAndLoad(template, context);
        resolvedExpressionsAreReplaced(document);
        unresolvedExpressionsAreNotReplaced(document);
    }

    private void resolvedExpressionsAreReplaced(WordprocessingMLPackage document) {
        P nameParagraph = (P) document.getMainDocumentPart().getContent().get(2);
        Assert.assertEquals("In this paragraph, the variable „name“ should be resolved to the value Homer Simpson.", new ParagraphWrapper(nameParagraph).getText());
    }

    private void unresolvedExpressionsAreNotReplaced(WordprocessingMLPackage document) {
        P fooParagraph = (P) document.getMainDocumentPart().getContent().get(3);
        Assert.assertEquals("In this paragraph, the variable „foo“ should not be resolved: ${foo}.", new ParagraphWrapper(fooParagraph).getText());
    }


}
