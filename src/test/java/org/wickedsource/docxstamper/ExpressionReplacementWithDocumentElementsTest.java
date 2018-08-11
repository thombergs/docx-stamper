package org.wickedsource.docxstamper;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.docx4j.TextUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.Br;
import org.docx4j.wml.ContentAccessor;
import org.junit.Test;
import org.wickedsource.docxstamper.api.DocxStamperException;
import org.wickedsource.docxstamper.api.typeresolver.ITypeResolver;

import com.google.common.collect.Lists;

public class ExpressionReplacementWithDocumentElementsTest extends AbstractDocx4jTest {

    private static final String TEST_FILE = "ExpressionReplacementWithDocumentElements.docx";
    private static final String BAD_TEST_FILE =
            "ExpressionReplacementWithDocumentElements-Invalid.docx";

    @Test
    public void test() throws Exception {
        test(TEST_FILE);
    }

    @Test(expected = DocxStamperException.class)
    public void testInvalidFile() throws Exception {
        test(BAD_TEST_FILE);
    }

    private void test(String testFile) throws Exception {
        InputStream template = getClass().getResourceAsStream(testFile);
        DocxStamperConfiguration config = new DocxStamperConfiguration();
        config.addTypeResolver(MainDocumentPart.class, new DocumentContentResolver());
        WordprocessingMLPackage stamped = stampAndLoad(template, new TestContext(), config);

        assertPagesMatch(stamped);
    }

    /**
     * tests that the content on the first page of the test document was copied to the
     * placeholder location on the second page
     * 
     * @param stamped
     * @throws Exception
     */
    private void assertPagesMatch(WordprocessingMLPackage stamped) throws Exception {
        List<Object> content = stamped.getMainDocumentPart().getContent();
        boolean inFirstPage = true;
        ArrayList<Object> page1 = Lists.newArrayList();
        ArrayList<Object> page2 = Lists.newArrayList();
        for (Object o : content) {
            if (hasPageBreak(o)) {
                inFirstPage = false;
                continue;
            }
            if (inFirstPage) {
                page1.add(o);
            } else {
                page2.add(o);
            }
        }
        assertEquals(13, page1.size());
        List<Object> expected = loadDocument(TEST_FILE).getMainDocumentPart().getContent();
        for (int i = 0; i < page1.size(); i++) {
            assertEquals(extractText(expected.get(i)), extractText(page1.get(i)));
            assertEquals(extractText(expected.get(i)), extractText(page2.get(i)));
        }
        return;
    }

    private String extractText(Object e) throws Exception {
        StringWriter w = new StringWriter();
        TextUtils.extractText(e, w);
        return w.toString();
    }

    private boolean hasPageBreak(Object o) {
        if (o instanceof Br) {
            return true;
        }
        if (!(o instanceof ContentAccessor)) {
            return false;
        }
        for (Object c : ((ContentAccessor) o).getContent()) {
            if (hasPageBreak(c)) {
                return true;
            }
        }
        return false;
    }

    private final class DocumentContentResolver
            implements ITypeResolver<MainDocumentPart, Collection<?>> {
        public Collection<?> resolve(WordprocessingMLPackage document,
                MainDocumentPart expressionResult) {
            List<Object> content = expressionResult.getContent();
            // leave the placeholder paragraph out of the replacement content
            content.remove(content.size() - 1);
            return content;
        }
    }

    public class TestContext {

        private WordprocessingMLPackage doc;

        TestContext() throws Docx4JException {
            doc = loadDocument(TEST_FILE);
        }

        public MainDocumentPart getCopiedElements() {
            return doc.getMainDocumentPart();
        }
    }

}
