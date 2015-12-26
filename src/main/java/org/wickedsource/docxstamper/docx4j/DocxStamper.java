package org.wickedsource.docxstamper.docx4j;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.wickedsource.docxstamper.DocxStamperException;
import org.wickedsource.docxstamper.el.ExpressionResolver;
import org.wickedsource.docxstamper.poi.commentprocessing.CommentProcessorRegistry;
import org.wickedsource.docxstamper.poi.commentprocessing.ContextFactory;
import org.wickedsource.docxstamper.poi.commentprocessing.displayif.DisplayIfProcessor;
import org.wickedsource.docxstamper.poi.commentprocessing.displayif.IDisplayIfProcessor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DocxStamper<T> {

    private ContextFactory<T> contextFactory = new ContextFactory<>();

    private PlaceholderReplacer<T> placeholderReplacer = new PlaceholderReplacer<>();

    private CommentProcessorRegistry commentProcessorRegistry = new CommentProcessorRegistry();

    private ExpressionResolver expressionResolver = new ExpressionResolver();

    public DocxStamper() {
        commentProcessorRegistry.registerCommentProcessor(IDisplayIfProcessor.class, new DisplayIfProcessor());
    }

    public void stamp(InputStream template, T contextRoot, OutputStream out) throws IOException, Docx4JException {
        WordprocessingMLPackage document = WordprocessingMLPackage.load(template);
        stamp(document, contextRoot, out);
    }

    public void stamp(WordprocessingMLPackage document, T contextRoot, OutputStream out) {
        try {
            T contextRootProxy = contextFactory.createProxy(contextRoot, commentProcessorRegistry);
            replaceExpressions(document, contextRootProxy);
            processComments(document, contextRootProxy);
            document.save(out);
        } catch (Exception e) {
            throw new DocxStamperException(e);
        }
    }

    private void replaceExpressions(WordprocessingMLPackage document, T contextRoot) {
        placeholderReplacer.resolveExpressions(document, contextRoot);
    }

    private void processComments(WordprocessingMLPackage document, final T contextRoot) {
        // TODO
    }

    public CommentProcessorRegistry getCommentProcessorRegistry() {
        return commentProcessorRegistry;
    }
}
