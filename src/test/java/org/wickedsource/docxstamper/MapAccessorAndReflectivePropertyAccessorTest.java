package org.wickedsource.docxstamper;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Text;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.expression.MapAccessor;

import javax.xml.bind.JAXBElement;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapAccessorAndReflectivePropertyAccessorTest extends AbstractDocx4jTest {
    class Container {
        public String value;

        public Container(String value) {
            this.value = value;
        }
    }

    @Test
    public void shouldResolveMapAndPropertyPlaceholders() throws Docx4JException, IOException {
        List<Container> listProp = new ArrayList<>();
        listProp.add(new Container("first value"));
        listProp.add(new Container("second value"));

        Map<String, Object> context = new HashMap<>();
        context.put("FLAT_STRING", "Flat string has been resolved");
        context.put("OBJECT_LIST_PROP", listProp);

        DocxStamperConfiguration config = new DocxStamperConfiguration()
                .setFailOnUnresolvedExpression(false)
                .setLineBreakPlaceholder("\n")
                .replaceNullValues(true)
                .nullValuesDefault("N/C")
                .replaceUnresolvedExpressions(true)
                .unresolvedExpressionsDefaultValue("N/C")
                .setEvaluationContextConfigurer(ctx -> ctx.addPropertyAccessor(new MapAccessor()));

        InputStream template = getClass().getResourceAsStream("MapAccessorAndReflectivePropertyAccessorTest.docx");
        WordprocessingMLPackage result = stampAndLoad(template, context, config);
        List<Object> contents = result.getMainDocumentPart().getContent();

        Assert.assertEquals("Flat string has been resolved", ((Text) ((JAXBElement) ((R) ((P) contents.get(0)).getContent().get(1)).getContent().get(0)).getValue()).getValue());
        Assert.assertEquals(3, ((Tbl) ((JAXBElement) contents.get(2)).getValue()).getContent().size());
        Assert.assertEquals("first value", ((Text) ((JAXBElement) ((R) ((P) contents.get(6)).getContent().get(0)).getContent().get(0)).getValue()).getValue());
        Assert.assertEquals("second value", ((Text) ((JAXBElement) ((R) ((P) contents.get(9)).getContent().get(0)).getContent().get(0)).getValue()).getValue());
    }
}
