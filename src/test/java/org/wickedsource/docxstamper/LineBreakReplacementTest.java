package org.wickedsource.docxstamper;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Br;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.junit.Assert;
import org.junit.Test;
import org.wickedsource.docxstamper.context.NameContext;
import org.wickedsource.docxstamper.util.ParagraphWrapper;

import java.io.IOException;
import java.io.InputStream;

public class LineBreakReplacementTest extends AbstractDocx4jTest {

    @Test
    public void test() throws Docx4JException, IOException {
        NameContext context = new NameContext(null);
        DocxStamperConfiguration config = new DocxStamperConfiguration();
        config.setLineBreakPlaceholder("#");
        InputStream template = getClass().getResourceAsStream("LineBreakReplacementTest.docx");
        WordprocessingMLPackage document = stampAndLoad(template, context, config);
        lineBreaksAreReplaced(document);
    }

    private void lineBreaksAreReplaced(WordprocessingMLPackage document) {
        P paragraph = (P) document.getMainDocumentPart().getContent().get(2);
        Assert.assertTrue(new ParagraphWrapper(paragraph).getText().contains("This paragraph should be"));
        Assert.assertTrue(new ParagraphWrapper(paragraph).getText().contains("split in"));
        Assert.assertTrue(new ParagraphWrapper(paragraph).getText().contains("three lines"));

        Assert.assertEquals(R.class, paragraph.getContent().get(1).getClass());
        Assert.assertEquals(Br.class, ((R)paragraph.getContent().get(1)).getContent().get(0).getClass());

        Assert.assertEquals(R.class, paragraph.getContent().get(3).getClass());
        Assert.assertEquals(Br.class, ((R)paragraph.getContent().get(3)).getContent().get(0).getClass());
    }

}
