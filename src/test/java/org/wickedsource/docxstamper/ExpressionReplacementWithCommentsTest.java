package org.wickedsource.docxstamper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.junit.Assert;
import org.junit.Test;
import org.wickedsource.docxstamper.context.NameContext;
import org.wickedsource.docxstamper.util.ParagraphWrapper;

public class ExpressionReplacementWithCommentsTest extends AbstractDocx4jTest {

	@Test
    public void test() throws Docx4JException, IOException {
        NameContext context = new NameContext();
        context.setName("Homer Simpson");
        InputStream template = getClass().getResourceAsStream("ExpressionReplacementWithCommentsTest.docx");
        OutputStream out = getOutputStream();
		DocxStamper stamper = new DocxStamper();
		stamper.getCommentProcessorRegistry().setFailOnInvalidExpression(false);
		stamper.stamp(template, context, out);
		InputStream in = getInputStream(out);
		WordprocessingMLPackage document = WordprocessingMLPackage.load(in);
        resolvedExpressionsAreReplaced(document);
        unresolvedExpressionsAreNotReplaced(document);
    }

    private void resolvedExpressionsAreReplaced(WordprocessingMLPackage document) {
        P nameParagraph = (P) document.getMainDocumentPart().getContent().get(2);
        Assert.assertEquals("In this paragraph, the variable name should be resolved to the value Homer Simpson.", new ParagraphWrapper(nameParagraph).getText());
    }

    private void unresolvedExpressionsAreNotReplaced(WordprocessingMLPackage document) {
        P fooParagraph = (P) document.getMainDocumentPart().getContent().get(3);
        Assert.assertEquals("In this paragraph, the variable foo should not be resolved: unresolvedValueWithComment.", new ParagraphWrapper(fooParagraph).getText());
    }


}
