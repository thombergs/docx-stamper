package io.reflectoring.docxstamper;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.junit.Assert;
import org.junit.Test;
import io.reflectoring.docxstamper.context.NameContext;
import io.reflectoring.docxstamper.util.ParagraphWrapper;

import java.io.IOException;
import java.io.InputStream;

public class TabsIndendationTest extends AbstractDocx4jTest {

  @Test
  public void tabsArePreserved() throws Docx4JException, IOException {
    NameContext context = new NameContext();
    context.setName("Homer Simpson");
    InputStream template = getClass().getResourceAsStream("TabsIntendationTest.docx");
    WordprocessingMLPackage document = stampAndLoad(template, context);

    P nameParagraph = (P) document.getMainDocumentPart().getContent().get(0);
    Assert.assertEquals("Tab\tHomer Simpson", new ParagraphWrapper(nameParagraph).getText());
  }

  @Test
  public void whiteSpacesArePreserved() throws Docx4JException, IOException {
    NameContext context = new NameContext();
    context.setName("Homer Simpson");
    InputStream template = getClass().getResourceAsStream("TabsIntendationTest.docx");
    WordprocessingMLPackage document = stampAndLoad(template, context);

    P nameParagraph = (P) document.getMainDocumentPart().getContent().get(1);
    Assert.assertEquals("Space Homer Simpson", new ParagraphWrapper(nameParagraph).getText());
  }


}