package pro.verron.docxstamper;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.wickedsource.docxstamper.api.DocxStamperException;
import org.wickedsource.docxstamper.replace.typeresolver.AbstractToTextResolver;
import pro.verron.docxstamper.utils.TestDocxStamper;
import pro.verron.docxstamper.utils.context.Contexts;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SpelInjectionTest {

    @Test
    public void test() throws Docx4JException, IOException {
        var context = Contexts.empty();
        var template = getClass().getResourceAsStream("SpelInjectionTest.docx");
        assertThrows(
                DocxStamperException.class,
                () -> new TestDocxStamper<>().stampAndLoadAndExtract(template, context)
        );
        Assertions.assertDoesNotThrow(()->"Does not throw", "Since VM is still up.");
    }
}
