package org.wickedsource.docxstamper;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.junit.Assert;
import org.junit.Test;
import org.wickedsource.docxstamper.context.NameContext;
import org.wickedsource.docxstamper.util.ParagraphWrapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class LeaveEmptyOnExpressionErrorTest extends AbstractDocx4jTest {

  @Test
  public void test() throws Docx4JException, IOException {
    NameContext context = new NameContext("Homer Simpson");
    InputStream template = getClass().getResourceAsStream("LeaveEmptyOnExpressionErrorTest.docx");
    OutputStream out = getOutputStream();
    DocxStamper<NameContext> stamper = new DocxStamperConfiguration()
            .leaveEmptyOnExpressionError(true)
            .build();
    stamper.stamp(template, context, out);
    InputStream in = getInputStream(out);
    WordprocessingMLPackage document = WordprocessingMLPackage.load(in);
    resolvedExpressionsAreReplaced(document);
  }

  private void resolvedExpressionsAreReplaced(WordprocessingMLPackage document) {
    P nameParagraph = (P) document.getMainDocumentPart().getContent().get(0);
    Assert.assertEquals("Leave me empty .", new ParagraphWrapper(nameParagraph).getText());
  }
}
