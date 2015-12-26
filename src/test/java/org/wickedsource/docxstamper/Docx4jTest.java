package org.wickedsource.docxstamper;

import org.docx4j.TextUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.P;
import org.junit.Test;

import java.io.StringWriter;
import java.util.List;

public class Docx4jTest {

    @Test
    public void test() throws Exception {
        WordprocessingMLPackage document = WordprocessingMLPackage.load(getClass().getResourceAsStream("/template.docx"));
        MainDocumentPart mainDocumentPart = document.getMainDocumentPart();
//        mainDocumentPart.variableReplace();
        List<Object> documentContent = mainDocumentPart.getContent();

        for (Object contentObject : documentContent) {
            if (contentObject instanceof P) {
                P paragraph = (P) contentObject;
                StringWriter writer = new StringWriter();
                TextUtils.extractText(paragraph, writer);
                System.out.println(writer.getBuffer());
            }
        }
    }
}
