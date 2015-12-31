package org.wickedsource.docxstamper.docx4j.processor;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.spel.SpelEvaluationException;
import org.wickedsource.docxstamper.DocxStamperException;
import org.wickedsource.docxstamper.docx4j.util.CommentUtil;
import org.wickedsource.docxstamper.docx4j.walk.coordinates.BaseCoordinatesWalker;
import org.wickedsource.docxstamper.docx4j.walk.coordinates.CoordinatesWalker;
import org.wickedsource.docxstamper.docx4j.walk.coordinates.ParagraphCoordinates;
import org.wickedsource.docxstamper.el.ExpressionResolver;
import org.wickedsource.docxstamper.proxy.ContextFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Allows registration of ICommentProcessor objects. Each registered ICommentProcessor must implement an interface
 * which has to be specified at registration time. Provides several getter methods to access the registered ICommentProcessors.
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
     * Lets each registered ICommentProcessor have a run on the specified docx document. At the end of the document
     * the commit method is called for each ICommentProcessor. The ICommentProcessors are run in the order they were
     * registered.
     *
     * @param document    the docx document over which to run the registered ICommentProcessors.
     * @param contextRoot the context root object against which to resolve expressions within the comments.
     * @param <T>         type of the contextRoot object.
     */
    public <T> void runProcessors(final WordprocessingMLPackage document, final T contextRoot) {
        for (final ICommentProcessor processor : commentProcessors) {
            try {
                Class<?> commentProcessorInterface = commentProcessorInterfaces.get(processor);
                ContextFactory<T> contextFactory = new ContextFactory<>();
                final T contextRootProxy = contextFactory.createProxy(contextRoot, commentProcessorInterface, processor);
                runProcessor(document, processor, contextRootProxy);
            } catch (Exception e) {
                throw new DocxStamperException(String.format("Could not create a proxy around context root object of class %s", contextRoot.getClass()), e);
            }
        }
    }

    private <T> void runProcessor(final WordprocessingMLPackage document, final ICommentProcessor processor, final T contextRoot) {
        CoordinatesWalker walker = new BaseCoordinatesWalker(document) {
            @Override
            protected void onParagraph(ParagraphCoordinates paragraphCoordinates) {
                try {
                    processor.setCurrentParagraphCoordinates(paragraphCoordinates);
                    String comment = CommentUtil.getCommentFor(paragraphCoordinates.getParagraph(), document);
                    if (comment != null) {
                        try {
                            expressionResolver.resolveExpression(comment, contextRoot);
                            logger.debug(String.format("Comment '%s' has been successfully processed by comment processor %s.", comment, processor.getClass()));
                        } catch (SpelEvaluationException e) {
                            logger.debug(String.format("Skipping comment '%s' because it can not be resolved by comment processor %s.", comment, processor.getClass()));
                        }
                    }
                    // TODO: remove comment once it was successfully processed.
                } catch (Docx4JException e) {
                    logger.warn(String.format("Skipping comment because comment could not be loaded for paragraph at coordinates: %s", paragraphCoordinates.toString()), e);
                }
            }
        };
        walker.walk();
        processor.commitChanges(document);
        logger.info(String.format("Finished run of comment processor %s", processor.getClass()));
    }

}
