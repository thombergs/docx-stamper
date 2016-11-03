package org.wickedsource.docxstamper;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.junit.Assert;
import org.junit.Test;
import org.wickedsource.docxstamper.context.NameContext;
import org.wickedsource.docxstamper.replace.ParagraphWrapper;

import java.io.IOException;
import java.io.InputStream;

public class TernaryOperatorTest extends AbstractDocx4jTest {

    @Test
    public void test() throws Docx4JException, IOException {
        NameContext context = new NameContext();
        context.setName("Homer");
        InputStream template = getClass().getResourceAsStream("TernaryOperatorTest.docx");
        WordprocessingMLPackage document = stampAndLoad(template, context);
        resolvedExpressionsAreReplaced(document);
        unresolvedExpressionsAreNotReplaced(document);
    }

    private void resolvedExpressionsAreReplaced(WordprocessingMLPackage document) {
        P nameParagraph = (P) document.getMainDocumentPart().getContent().get(3);
        Assert.assertEquals("Homer <-- this should read \"Homer\".", new ParagraphWrapper(nameParagraph).getText());
    }

    private void unresolvedExpressionsAreNotReplaced(WordprocessingMLPackage document) {
        P fooParagraph = (P) document.getMainDocumentPart().getContent().get(4);
        Assert.assertEquals("<-- this should be empty.", new ParagraphWrapper(fooParagraph).getText().trim());
    }


}
