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

public class LineBreakReplacementTest extends AbstractDocx4jTest {

    @Test
    public void test() throws Docx4JException, IOException {
        NameContext context = new NameContext();
        DocxStamperConfiguration config = new DocxStamperConfiguration();
        config.setLineBreakPlaceholder("#");
        InputStream template = getClass().getResourceAsStream("LineBreakReplacementTest.docx");
        WordprocessingMLPackage document = stampAndLoad(template, context, config);
        lineBreaksAreReplaced(document);
    }

    private void lineBreaksAreReplaced(WordprocessingMLPackage document) {
        P paragraph = (P) document.getMainDocumentPart().getContent().get(2);
        Assert.assertTrue(new ParagraphWrapper(paragraph).getText().contains("This paragraph should be"));
        Assert.assertTrue(new ParagraphWrapper(paragraph).getText().contains("split in two lines"));
        // This test does NOT assert that there is a line break, since I cannot find a way to find out if there
        // is a line break or not.
    }

}
