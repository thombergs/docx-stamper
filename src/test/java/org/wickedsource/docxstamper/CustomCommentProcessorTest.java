package org.wickedsource.docxstamper;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.Assert;
import org.junit.Test;
import org.wickedsource.docxstamper.api.commentprocessor.ICommentProcessor;
import org.wickedsource.docxstamper.api.coordinates.ParagraphCoordinates;
import org.wickedsource.docxstamper.api.coordinates.RunCoordinates;
import org.wickedsource.docxstamper.proxy.ProxyBuilder;
import org.wickedsource.docxstamper.util.CommentWrapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class CustomCommentProcessorTest extends AbstractDocx4jTest {

    @Test
    public void test() throws Docx4JException, IOException {
        CustomCommentProcessor processor = new CustomCommentProcessor();
        DocxStamperConfiguration config = new DocxStamperConfiguration()
                .addCommentProcessor(ICustomCommentProcessor.class, processor);
        InputStream template = getClass().getResourceAsStream("CustomCommentProcessorTest.docx");
        stampAndLoad(template, new EmptyContext(), config);
        Assert.assertEquals(2, processor.getVisitedParagraphs().size());
    }

    static class EmptyContext{

    }

    public interface ICustomCommentProcessor {

        void visitParagraph();

    }

    public static class CustomCommentProcessor implements ICommentProcessor, ICustomCommentProcessor{

        private List<ParagraphCoordinates> visitedParagraphs = new ArrayList<>();

        private ParagraphCoordinates currentParagraph;

        @Override
        public void commitChanges(WordprocessingMLPackage document) {

        }

        @Override
        public void setCurrentParagraphCoordinates(ParagraphCoordinates coordinates) {
            currentParagraph = coordinates;
        }

        @Override
        public void setCurrentRunCoordinates(RunCoordinates coordinates) {

        }

        @Override
        public void setCurrentCommentWrapper(CommentWrapper commentWrapper) {

        }

        @Override
        public void setProxyBuilder(ProxyBuilder proxyBuilder) {

        }

        @Override
        public void reset() {

        }

        public List<ParagraphCoordinates> getVisitedParagraphs() {
            return visitedParagraphs;
        }

        @Override
        public void visitParagraph() {
            visitedParagraphs.add(currentParagraph);
        }
    }


}
