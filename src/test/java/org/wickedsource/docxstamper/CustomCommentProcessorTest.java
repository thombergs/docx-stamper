package org.wickedsource.docxstamper;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.junit.Assert;
import org.junit.Test;
import org.wickedsource.docxstamper.api.commentprocessor.ICommentProcessor;
import org.wickedsource.docxstamper.api.typeresolver.TypeResolverRegistry;
import org.wickedsource.docxstamper.processor.BaseCommentProcessor;
import org.wickedsource.docxstamper.util.CommentWrapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class CustomCommentProcessorTest extends AbstractDocx4jTest {

    @Test
    public void test() throws Docx4JException, IOException {
        DocxStamperConfiguration config = new DocxStamperConfiguration()
                .addCommentProcessor(ICustomCommentProcessor.class, CustomCommentProcessor.class);
        InputStream template = getClass().getResourceAsStream("CustomCommentProcessorTest.docx");
        OutputStream out = getOutputStream();
        DocxStamper<EmptyContext> stamper = new DocxStamper<>(config);
        stamper.stamp(template, new EmptyContext(), out);
        CustomCommentProcessor processor = (CustomCommentProcessor) stamper.getCommentProcessorInstance(ICustomCommentProcessor.class);
        Assert.assertEquals(2, processor.getVisitedParagraphs().size());
    }

    static class EmptyContext {

    }

    public interface ICustomCommentProcessor extends ICommentProcessor {

        void visitParagraph();

    }

    public static class CustomCommentProcessor extends BaseCommentProcessor implements ICustomCommentProcessor {

        private final List<P> visitedParagraphs = new ArrayList<>();

        private P currentParagraph;

        public CustomCommentProcessor(DocxStamperConfiguration config, TypeResolverRegistry typeResolverRegistry) {
            super(config, typeResolverRegistry);
        }

        @Override
        public void commitChanges(WordprocessingMLPackage document) {

        }

        @Override
        public void setParagraph(P paragraph) {
            currentParagraph = paragraph;
        }

        @Override
        public void setCurrentRun(R run) {

        }

        @Override
        public void setCurrentCommentWrapper(CommentWrapper commentWrapper) {

        }

        @Override
        public void setDocument(WordprocessingMLPackage document) {

        }

        @Override
        public void reset() {

        }

        public List<P> getVisitedParagraphs() {
            return visitedParagraphs;
        }

        @Override
        public void visitParagraph() {
            visitedParagraphs.add(currentParagraph);
        }
    }


}
