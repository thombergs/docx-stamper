package org.wickedsource.docxstamper.util;

import java.io.IOException;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.junit.Assert;
import org.junit.Test;
import org.wickedsource.docxstamper.AbstractDocx4jTest;

public class RunUtilTest extends AbstractDocx4jTest {

    @Test
    public void getTextReturnsTextOfRun() throws Docx4JException {
        WordprocessingMLPackage document = loadDocument("singleRun.docx");
        P paragraph = (P) document.getMainDocumentPart().getContent().get(0);
        R run = (R) paragraph.getContent().get(0);
        Assert.assertEquals("This is the only run of text in this document.", RunUtil.getText(run));
    }

    @Test
    public void getTextReturnsValueDefinedBySetText() throws Docx4JException, IOException {
        WordprocessingMLPackage document = loadDocument("singleRun.docx");
        P paragraph = (P) document.getMainDocumentPart().getContent().get(0);
        R run = (R) paragraph.getContent().get(0);
        RunUtil.setText(run, "The text of this run was changed.");
        document = saveAndLoadDocument(document);
        paragraph = (P) document.getMainDocumentPart().getContent().get(0);
        run = (R) paragraph.getContent().get(0);
        Assert.assertEquals("The text of this run was changed.", RunUtil.getText(run));
    }

}