package org.wickedsource.docxstamper.api.commentprocessor;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Comments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.SpelParseException;
import org.wickedsource.docxstamper.api.DocxStamperException;
import org.wickedsource.docxstamper.el.ExpressionResolver;
import org.wickedsource.docxstamper.proxy.ContextFactory;
import org.wickedsource.docxstamper.util.CommentUtil;
import org.wickedsource.docxstamper.util.CommentWrapper;
import org.wickedsource.docxstamper.walk.coordinates.BaseCoordinatesWalker;
import org.wickedsource.docxstamper.walk.coordinates.CoordinatesWalker;
import org.wickedsource.docxstamper.walk.coordinates.ParagraphCoordinates;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Allows registration of ICommentProcessor objects. Each registered ICommentProcessor must implement an interface which has to be specified at registration
 * time. Provides several getter methods to access the registered ICommentProcessors.
 */
public class CommentProcessorRegistry {

    private Logger logger = LoggerFactory.getLogger(CommentProcessorRegistry.class);

    private Map<ICommentProcessor, Class<?>> commentProcessorInterfaces = new HashMap<>();

    private List<ICommentProcessor> commentProcessors = new ArrayList<>();

    private ExpressionResolver expressionResolver = new ExpressionResolver();

    /**
     * Registers the specified ICommentProcessor as an implementation of the specified interface.
     *
     * @param interfaceClass   the Interface which is implemented by the commentProcessor.
     * @param commentProcessor the commentProcessor implementing the specified interface.
     */
    public void registerCommentProcessor(Class<?> interfaceClass, ICommentProcessor commentProcessor) {
        this.commentProcessorInterfaces.put(commentProcessor, interfaceClass);
        this.commentProcessors.add(commentProcessor);
    }

    /**
     * Lets each registered ICommentProcessor have a run on the specified docx document. At the end of the document the commit method is called for each
     * ICommentProcessor. The ICommentProcessors are run in the order they were registered.
     *
     * @param document    the docx document over which to run the registered ICommentProcessors.
     * @param contextRoot the context root object against which to resolve expressions within the comments.
     * @param <T>         type of the contextRoot object.
     */
    public <T> void runProcessors(final WordprocessingMLPackage document, final T contextRoot) {
        Map<BigInteger, CommentWrapper> comments = CommentUtil.getComments(document);
        for (final ICommentProcessor processor : commentProcessors) {
            try {
                Class<?> commentProcessorInterface = commentProcessorInterfaces.get(processor);
                ContextFactory<T> contextFactory = new ContextFactory<>();
                final T contextRootProxy = contextFactory.createProxy(contextRoot, commentProcessorInterface, processor);
                runProcessor(document, processor, comments, contextRootProxy);
            } catch (Exception e) {
                throw new DocxStamperException(String.format("Could not create a proxy around context root object of class %s", contextRoot.getClass()), e);
            }
        }
    }

    private <T> void runProcessor(final WordprocessingMLPackage document, final ICommentProcessor processor, final Map<BigInteger, CommentWrapper> comments, final T contextRoot) {
        CoordinatesWalker walker = new BaseCoordinatesWalker(document) {

            @Override
            protected void onParagraph(ParagraphCoordinates paragraphCoordinates) {
                Comments.Comment comment = null;
                try {
                    processor.setCurrentParagraphCoordinates(paragraphCoordinates);
                    comment = CommentUtil.getCommentFor(paragraphCoordinates.getParagraph(), document);
                } catch (Docx4JException e) {
                    logger.warn(String.format("Skipping comment because comment could not be loaded for paragraph at coordinates: %s",
                            paragraphCoordinates.toString()), e);
                    return;
                }

                if (comment != null) {
                    CommentWrapper commentWrapper = comments.get(comment.getId());
                    String commentString = CommentUtil.getCommentString(comment);
                    try {
                        expressionResolver.resolveExpression(commentString, contextRoot);
                        CommentUtil.deleteComment(commentWrapper);
                        logger.debug(
                                String.format("Comment '%s' has been successfully processed by comment processor %s.", commentString, processor.getClass()));
                    } catch (SpelEvaluationException | SpelParseException e) {
                        logger.warn(String.format(
                                "Skipping comment expression '%s' because it can not be resolved by comment processor %s. Reason: %s. Set log level to TRACE to view Stacktrace.",
                                commentString, processor.getClass(), e.getMessage()));
                        logger.trace("Reason for skipping comment: ", e);
                    }
                }
            }
        };
        walker.walk();
        processor.commitChanges(document);
    }

}
