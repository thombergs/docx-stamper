package org.wickedsource.docxstamper;

import org.junit.jupiter.api.Test;
import org.wickedsource.docxstamper.api.DocxStamperException;
import pro.verron.docxstamper.utils.TestDocxStamper;
import pro.verron.docxstamper.utils.context.NullishContext;
import pro.verron.docxstamper.utils.context.SubContext;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.wickedsource.docxstamper.DefaultTests.getResource;

class NullPointerResolutionTest {
    @Test
    void nullPointerResolutionTest_testThrowingCase() throws IOException {
        var subContext = new SubContext("Fullish2",
                                        List.of("Fullish3", "Fullish4",
                                                "Fullish5"));
        var context = new NullishContext("Fullish1", subContext, null, null);
        try (var template = getResource("NullPointerResolution.docx")) {
            var stamper = new TestDocxStamper<NullishContext>(
                    new DocxStamperConfiguration());
            assertThrows(
                    DocxStamperException.class,
                    () -> stamper.stampAndLoadAndExtract(template, context)
            );
        }
    }
}
