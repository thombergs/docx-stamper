package org.wickedsource.docxstamper;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.Test;
import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RepeatDocPartWithImagesInSourceTest extends AbstractDocx4jTest {
    @Test
    public void shouldReplicateImageFromTheMainDocumentInTheSubTemplate() throws Docx4JException, IOException {
        Map<String, Object> context = new HashMap<>();
        ArrayList<Map<String, Object>> subDocParts = new ArrayList<>();

        Map<String, Object> firstPart = new HashMap<>();
        firstPart.put("name", "first doc part");
        subDocParts.add(firstPart);

        Map<String, Object> secondPart = new HashMap<>();
        secondPart.put("name", "second doc part");
        subDocParts.add(secondPart);

        context.put("subDocParts", subDocParts);

        DocxStamperConfiguration config = new DocxStamperConfiguration()
                .setEvaluationContextConfigurer((StandardEvaluationContext ctx) -> ctx.addPropertyAccessor(new MapAccessor()));

        InputStream template = getClass().getResourceAsStream("RepeatDocPartWithImagesInSourceTest.docx");
        WordprocessingMLPackage document = stampAndLoad(template, context, config);

        document.save(new File("RESULTAAAT.docx"));
    }
}
