package org.wickedsource.docxstamper;

import org.junit.Test;
import org.wickedsource.docxstamper.api.UnresolvedExpressionException;
import org.wickedsource.docxstamper.context.NameContext;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class FailOnUnresolvedExpressionTest extends AbstractDocx4jTest {

    @Test(expected = UnresolvedExpressionException.class)
    public void fails() {
        NameContext context = new NameContext("Homer");
        InputStream template = getClass().getResourceAsStream("FailOnUnresolvedExpressionTest.docx");
        DocxStamper<NameContext> stamper = new DocxStamper<>();
        stamper.stamp(template, context, new ByteArrayOutputStream());
    }

    @Test
    public void doesNotFail() {
        NameContext context = new NameContext("Homer");
        InputStream template = getClass().getResourceAsStream("FailOnUnresolvedExpressionTest.docx");
        DocxStamper<NameContext> stamper = new DocxStamperConfiguration()
                .setFailOnUnresolvedExpression(false)
                .build();
        stamper.stamp(template, context, new ByteArrayOutputStream());
        // no exception
    }

}
