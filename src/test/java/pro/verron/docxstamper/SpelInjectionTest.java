package pro.verron.docxstamper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.wickedsource.docxstamper.DocxStamperConfiguration;
import org.wickedsource.docxstamper.api.DocxStamperException;
import pro.verron.docxstamper.utils.TestDocxStamper;
import pro.verron.docxstamper.utils.context.Contexts;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.wickedsource.docxstamper.DefaultTests.getResource;

class SpelInjectionTest {

    @Test
    void spelInjectionTest() throws IOException {
        var context = Contexts.empty();
        try (var template = getResource("SpelInjectionTest.docx")) {
            var stamper = new TestDocxStamper<>(new DocxStamperConfiguration());
            assertThrows(DocxStamperException.class, () -> stamper.stampAndLoadAndExtract(template, context));
        }
        assertDoesNotThrow(() -> "Does not throw", "Since VM is still up.");
    }
}
