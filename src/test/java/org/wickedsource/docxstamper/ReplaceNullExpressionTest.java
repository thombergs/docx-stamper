package org.wickedsource.docxstamper;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.junit.jupiter.api.Test;
import org.wickedsource.docxstamper.util.ParagraphWrapper;
import pro.verron.docxstamper.utils.TestDocxStamper;
import pro.verron.docxstamper.utils.context.Contexts;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

public class ReplaceNullExpressionTest {
    @Test
    public void test() throws Docx4JException, IOException {
        var context = Contexts.name(null);
        var template = getClass().getResourceAsStream("ReplaceNullExpressionTest.docx");
        var config = new DocxStamperConfiguration().replaceNullValues(true);
        var actual = new TestDocxStamper<>(config).stampAndLoadAndExtract(template, context);

        var expected = List.of("I am .//rPr={}", "//rPr={u=single}");
        assertIterableEquals(expected, actual);
    }
}
