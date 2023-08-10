package io.reflectoring.docxstamper;

import java.io.IOException;
import java.io.InputStream;

import io.reflectoring.docxstamper.context.NameContext;
import io.reflectoring.docxstamper.util.ParagraphWrapper;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.junit.Assert;
import org.junit.Test;

public class CustomExpressionFunctionTest extends AbstractDocx4jTest {

  @Test
  public void test() throws Docx4JException, IOException {
    NameContext context = new NameContext();
    context.setName("Homer Simpson");
    InputStream template = getClass().getResourceAsStream("CustomExpressionFunction.docx");
    DocxStamperConfiguration config = new DocxStamperConfiguration()
            .exposeInterfaceToExpressionLanguage(UppercaseFunction.class, new UppercaseFunctionImpl());
    WordprocessingMLPackage document = stampAndLoad(template, context, config);
    P nameParagraph = (P) document.getMainDocumentPart().getContent().get(2);
    Assert.assertEquals("In this paragraph, a custom expression function is used to uppercase a String: HOMER SIMPSON.", new ParagraphWrapper(nameParagraph).getText());
    P commentedParagraph = (P) document.getMainDocumentPart().getContent().get(3);
    Assert.assertEquals("To test that custom functions work together with comment expressions, we toggle visibility of this paragraph with a comment expression.", new ParagraphWrapper(commentedParagraph).getText());
  }

  public interface UppercaseFunction {

    String toUppercase(String string);

  }

  public static class UppercaseFunctionImpl implements UppercaseFunction {

    @Override
    public String toUppercase(String string) {
      return string.toUpperCase();
    }
  }
}
