package org.wickedsource.docxstamper.docx4j;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wickedsource.docxstamper.DocxStamperException;
import org.wickedsource.docxstamper.docx4j.processor.CommentProcessorRegistry;
import org.wickedsource.docxstamper.docx4j.processor.ContextFactory;
import org.wickedsource.docxstamper.docx4j.processor.displayif.DisplayIfProcessor;
import org.wickedsource.docxstamper.docx4j.processor.displayif.IDisplayIfProcessor;
import org.wickedsource.docxstamper.docx4j.processor.repeat.IRepeatProcessor;
import org.wickedsource.docxstamper.docx4j.processor.repeat.RepeatProcessor;
import org.wickedsource.docxstamper.docx4j.util.CommentUtil;
import org.wickedsource.docxstamper.docx4j.walk.coordinates.BaseCoordinatesWalker;
import org.wickedsource.docxstamper.docx4j.walk.coordinates.CoordinatesWalker;
import org.wickedsource.docxstamper.docx4j.walk.coordinates.ParagraphCoordinates;
import org.wickedsource.docxstamper.el.ExpressionResolver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DocxStamper<T> {

    private Logger logger = LoggerFactory.getLogger(DocxStamper.class);

    private ContextFactory<T> contextFactory = new ContextFactory<>();

    private PlaceholderReplacer<T> placeholderReplacer = new PlaceholderReplacer<>();

    private CommentProcessorRegistry commentProcessorRegistry = new CommentProcessorRegistry();

    private ExpressionResolver expressionResolver = new ExpressionResolver();

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

    private void processComments(final WordprocessingMLPackage document, final T contextRoot) {
        CoordinatesWalker walker = new BaseCoordinatesWalker(document) {
            @Override
            protected void onParagraph(ParagraphCoordinates paragraphCoordinates) {
                try {
                    commentProcessorRegistry.setCurrentParagraphCoordinates(paragraphCoordinates);
                    String comment = CommentUtil.getCommentFor(paragraphCoordinates.getParagraph(), document);
                    if (comment != null) {
                        try {
                            expressionResolver.resolveExpression(comment, contextRoot);
                        } catch (Exception e) {
                            logger.warn(String.format("Skipping comment '%s' because it is not a valid expression.", comment));
                        }
                    }
                    // TODO: remove comment once it was successfully processed.
                } catch (Docx4JException e) {
                    logger.warn(String.format("Skipping comment because comment could not be loaded for paragraph at coordinates: %s", paragraphCoordinates.toString()), e);
                }
            }
        };
        walker.walk();
        commentProcessorRegistry.commitChanges(document);
    }

    public CommentProcessorRegistry getCommentProcessorRegistry() {
        return commentProcessorRegistry;
    }
}
