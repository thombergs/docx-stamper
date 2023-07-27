package pro.verron.docxstamper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.wickedsource.docxstamper.api.DocxStamperException;
import pro.verron.docxstamper.utils.TestDocxStamper;
import pro.verron.docxstamper.utils.context.Contexts;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;

class SpelInjectionTest {

    /***
     *
     * @throws IOException
     */

    @Test
    void test() throws IOException {

        var context = Contexts.empty();
        try (var template = getClass().getResourceAsStream("SpelInjectionTest.docx")) {
            var stamper = new TestDocxStamper<>();
            assertThrows(DocxStamperException.class, () -> stamper.stampAndLoadAndExtract(template, context));
        }
        Assertions.assertDoesNotThrow(() -> "Does not throw", "Since VM is still up.");
    }
}
