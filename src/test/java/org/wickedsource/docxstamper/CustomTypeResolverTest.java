package org.wickedsource.docxstamper;

import java.io.IOException;
import java.io.InputStream;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.junit.Assert;
import org.junit.Test;
import org.wickedsource.docxstamper.replace.typeresolver.AbstractToTextResolver;
import org.wickedsource.docxstamper.util.ParagraphWrapper;

public class CustomTypeResolverTest extends AbstractDocx4jTest {

    @Test
    public void test() throws Docx4JException, IOException {
        CustomTypeResolver resolver = new CustomTypeResolver();
        DocxStamperConfiguration config = new DocxStamperConfiguration()
                .addTypeResolver(CustomType.class, resolver);
        InputStream template = getClass().getResourceAsStream("CustomTypeResolverTest.docx");
        WordprocessingMLPackage document = stampAndLoad(template, new Context(), config);
        P nameParagraph = (P) document.getMainDocumentPart().getContent().get(2);
        Assert.assertEquals("The name should be resolved to foo.", new ParagraphWrapper(nameParagraph).getText());
    }

    public static class Context{

        private CustomType name = new CustomType();

        public CustomType getName() {
            return name;
        }

        public void setName(CustomType name) {
            this.name = name;
        }
    }

    public static class CustomType{

    }

    public static class CustomTypeResolver extends AbstractToTextResolver<CustomType>{

        @Override
        protected String resolveStringForObject(CustomType object) {
            return "foo";
        }

    }

}
