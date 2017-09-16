package org.wickedsource.docxstamper;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.junit.Test;
import org.wickedsource.docxstamper.api.UnresolvedExpressionException;
import org.wickedsource.docxstamper.context.NameContext;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FailOnUnresolvedExpressionTest extends AbstractDocx4jTest {

    @Test(expected = UnresolvedExpressionException.class)
    public void fails() throws Docx4JException, IOException {
        NameContext context = new NameContext();
        context.setName("Homer");
        InputStream template = getClass().getResourceAsStream("FailOnUnresolvedExpressionTest.docx");
        DocxStamper stamper = new DocxStamper();
        stamper.stamp(template, context, new ByteArrayOutputStream());
    }

    @Test
    public void doesNotFail() throws Docx4JException, IOException {
        NameContext context = new NameContext();
        context.setName("Homer");
        InputStream template = getClass().getResourceAsStream("FailOnUnresolvedExpressionTest.docx");
        DocxStamperConfiguration config = new DocxStamperConfiguration();
        config.setFailOnUnresolvedExpression(false);
        DocxStamper stamper = new DocxStamper(config);
        stamper.stamp(template, context, new ByteArrayOutputStream());
        // no exception
    }

}
