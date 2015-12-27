package org.wickedsource.docxstamper;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wickedsource.docxstamper.docx4j.PlaceholderReplacer;
import org.wickedsource.docxstamper.docx4j.processor.CommentProcessorRegistry;
import org.wickedsource.docxstamper.docx4j.processor.displayif.DisplayIfProcessor;
import org.wickedsource.docxstamper.docx4j.processor.displayif.IDisplayIfProcessor;
import org.wickedsource.docxstamper.docx4j.processor.repeat.IRepeatProcessor;
import org.wickedsource.docxstamper.docx4j.processor.repeat.RepeatProcessor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DocxStamper<T> {

    private Logger logger = LoggerFactory.getLogger(DocxStamper.class);

    private PlaceholderReplacer<T> placeholderReplacer = new PlaceholderReplacer<>();

    private CommentProcessorRegistry commentProcessorRegistry = new CommentProcessorRegistry();

    public DocxStamper() {
        commentProcessorRegistry.registerCommentProcessor(IRepeatProcessor.class, new RepeatProcessor());
        commentProcessorRegistry.registerCommentProcessor(IDisplayIfProcessor.class, new DisplayIfProcessor());
    }

    public void stamp(InputStream template, T contextRoot, OutputStream out) throws IOException, Docx4JException {
        WordprocessingMLPackage document = WordprocessingMLPackage.load(template);
        stamp(document, contextRoot, out);
    }

    public void stamp(WordprocessingMLPackage document, T contextRoot, OutputStream out) {
        try {
            replaceExpressions(document, contextRoot);
            processComments(document, contextRoot);
            document.save(out);
        } catch (Exception e) {
            throw new DocxStamperException(e);
        }
    }

    private void replaceExpressions(WordprocessingMLPackage document, T contextRoot) {
        placeholderReplacer.resolveExpressions(document, contextRoot);
    }

    private void processComments(final WordprocessingMLPackage document, final T contextRoot) {
        commentProcessorRegistry.runProcessors(document, contextRoot);
    }

    public CommentProcessorRegistry getCommentProcessorRegistry() {
        return commentProcessorRegistry;
    }
}
