package org.wickedsource.docxstamper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.wickedsource.docxstamper.api.UnresolvedExpressionException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertThrows;

class FailOnUnresolvedExpressionTest {
    @Test
    void fails() throws IOException {
        var context = new Name("Homer");
        try (var template = getClass().getResourceAsStream("FailOnUnresolvedExpressionTest.docx")) {
            var stamper = new DocxStamper<Name>();
            var outputStream = new ByteArrayOutputStream();
            assertThrows(UnresolvedExpressionException.class, () -> stamper.stamp(template, context, outputStream));
        }
    }

    @Test
    void doesNotFail() throws IOException {
        Name context = new Name("Homer");
        try (InputStream template = getClass().getResourceAsStream("FailOnUnresolvedExpressionTest.docx")) {
            var config = new DocxStamperConfiguration()
                    .setFailOnUnresolvedExpression(false);
            var stamper = new DocxStamper<Name>(config);
            Assertions.assertDoesNotThrow(() -> stamper.stamp(template, context, new ByteArrayOutputStream()));
        }
    }

    public record Name(String name) {
    }

}
