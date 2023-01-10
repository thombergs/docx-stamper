package org.wickedsource.docxstamper.processor;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Comments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.SpelParseException;
import org.wickedsource.docxstamper.DocxStamperConfiguration;
import org.wickedsource.docxstamper.api.UnresolvedExpressionException;
import org.wickedsource.docxstamper.api.commentprocessor.ICommentProcessor;
import org.wickedsource.docxstamper.api.coordinates.ParagraphCoordinates;
import org.wickedsource.docxstamper.api.coordinates.RunCoordinates;
import org.wickedsource.docxstamper.el.ExpressionResolver;
import org.wickedsource.docxstamper.el.ExpressionUtil;
import org.wickedsource.docxstamper.replace.PlaceholderReplacer;
import org.wickedsource.docxstamper.util.CommentUtil;
import org.wickedsource.docxstamper.util.CommentWrapper;
import org.wickedsource.docxstamper.util.ParagraphWrapper;
import org.wickedsource.docxstamper.util.walk.BaseCoordinatesWalker;
import org.wickedsource.docxstamper.util.walk.CoordinatesWalker;

import java.math.BigInteger;
import java.util.*;

/**
 * Allows registration of ICommentProcessor objects. Each registered
 * ICommentProcessor must implement an interface which has to be specified at
 * registration time. Provides several getter methods to access the registered
 * ICommentProcessors.
 */
public class CommentProcessorRegistry {

    private final Logger logger = LoggerFactory.getLogger(CommentProcessorRegistry.class);
    private final DocxStamperConfiguration configuration;

    private ExpressionResolver expressionResolver;

    private final ExpressionUtil expressionUtil = new ExpressionUtil();

    private final PlaceholderReplacer placeholderReplacer;

    public CommentProcessorRegistry(PlaceholderReplacer placeholderReplacer, DocxStamperConfiguration configuration) {
        this.placeholderReplacer = placeholderReplacer;
        this.configuration = configuration;
        this.expressionResolver = new ExpressionResolver(configuration);
    }

    public void setExpressionResolver(ExpressionResolver expressionResolver) {
        this.expressionResolver = expressionResolver;
    }

    /**
     * Lets each registered ICommentProcessor have a run on the specified docx
     * document. At the end of the document the commit method is called for each
     * ICommentProcessor. The ICommentProcessors are run in the order they were
     * registered.
     *
     * @param document          the docx document over which to run the registered ICommentProcessors.
     * @param expressionContext the context root object
     */
    public <T> void runProcessors(final WordprocessingMLPackage document, final T expressionContext) {
        final Map<BigInteger, CommentWrapper> comments = CommentUtil.getComments(document);
        final List<CommentWrapper> proceedComments = new ArrayList<>();

        CoordinatesWalker walker = new BaseCoordinatesWalker(document) {

            @Override
            protected void onParagraph(ParagraphCoordinates paragraphCoordinates) {
                runProcessorsOnParagraphComment(document, comments, expressionContext, paragraphCoordinates)
                        .ifPresent(proceedComments::add);
                runProcessorsOnInlineContent(expressionContext, paragraphCoordinates);
            }

            @Override
            protected void onRun(RunCoordinates runCoordinates,
                                 ParagraphCoordinates paragraphCoordinates) {
                runProcessorsOnRunComment(document, comments, expressionContext, paragraphCoordinates, runCoordinates)
                        .ifPresent(proceedComments::add);
            }

        };
        walker.walk();

        for (Object processor : configuration.getCommentProcessors().values()) {
            ((ICommentProcessor) processor).commitChanges(document);
        }
        for (CommentWrapper commentWrapper : proceedComments) {
            CommentUtil.deleteComment(commentWrapper);
        }

    }

    /**
     * Finds all processor expressions within the specified paragraph and tries
     * to evaluate it against all registered {@link ICommentProcessor}s.
     *
     * @param expressionContext    a builder for a proxy around the context root object to customize its interface
     * @param paragraphCoordinates the paragraph to process.
     * @param <T>                  type of the context root object
     */
    private <T> void runProcessorsOnInlineContent(T expressionContext,
                                                  ParagraphCoordinates paragraphCoordinates) {

        ParagraphWrapper paragraph = new ParagraphWrapper(paragraphCoordinates.getParagraph());
        List<String> processorExpressions = expressionUtil
                .findProcessorExpressions(paragraph.getText());

        for (String processorExpression : processorExpressions) {
            String strippedExpression = expressionUtil.stripExpression(processorExpression);

            for (final Object processor : configuration.getCommentProcessors().values()) {
                ((ICommentProcessor) processor).setCurrentParagraphCoordinates(paragraphCoordinates);
            }

            try {
                expressionResolver.resolveExpression(strippedExpression, expressionContext);
                placeholderReplacer.replace(paragraph, processorExpression, null);
                logger.debug(String.format(
                        "Processor expression '%s' has been successfully processed by a comment processor.",
                        processorExpression));
            } catch (SpelEvaluationException | SpelParseException e) {
                if (configuration.isFailOnUnresolvedExpression()) {
                    throw new UnresolvedExpressionException(strippedExpression, e);
                } else {
                    logger.warn(String.format(
                            "Skipping processor expression '%s' because it can not be resolved by any comment processor. Reason: %s. Set log level to TRACE to view Stacktrace.",
                            processorExpression, e.getMessage()));
                    logger.trace("Reason for skipping processor expression: ", e);
                }
            }
        }
    }


    /**
     * Takes the first comment on the specified paragraph and tries to evaluate
     * the string within the comment against all registered
     * {@link ICommentProcessor}s.
     *
     * @param document             the word document.
     * @param comments             the comments within the document.
     * @param expressionContext    the context root object
     * @param paragraphCoordinates the paragraph whose comments to evaluate.
     * @param <T>                  the type of the context root object.
     */
    private <T> Optional<CommentWrapper> runProcessorsOnParagraphComment(final WordprocessingMLPackage document,
                                                                         final Map<BigInteger, CommentWrapper> comments, T expressionContext,
                                                                         ParagraphCoordinates paragraphCoordinates) {
        Comments.Comment comment = CommentUtil.getCommentFor(paragraphCoordinates.getParagraph(), document);
        return runCommentProcessors(comments, expressionContext, comment, paragraphCoordinates, null);
    }

    private <T> Optional<CommentWrapper> runProcessorsOnRunComment(final WordprocessingMLPackage document,
                                                                   final Map<BigInteger, CommentWrapper> comments, T expressionContext,
                                                                   ParagraphCoordinates paragraphCoordinates, RunCoordinates runCoordinates) {
        Comments.Comment comment = CommentUtil.getCommentAround(runCoordinates.getRun(), document);
        return runCommentProcessors(comments, expressionContext, comment, paragraphCoordinates, runCoordinates);
    }

    private <T> Optional<CommentWrapper> runCommentProcessors(final Map<BigInteger, CommentWrapper> comments, T expressionContext,
                                                              Comments.Comment comment, ParagraphCoordinates paragraphCoordinates,
                                                              RunCoordinates runCoordinates) {

        CommentWrapper commentWrapper = Optional.ofNullable(comment)
                .map(Comments.Comment::getId)
                .map(comments::get)
                .orElse(null);

        if (Objects.isNull(comment) || Objects.isNull(commentWrapper)) {
            // no comment to process
            return Optional.empty();
        }

        String commentString = CommentUtil.getCommentString(comment);

        for (final Object processor : configuration.getCommentProcessors().values()) {
            ((ICommentProcessor) processor).setCurrentParagraphCoordinates(paragraphCoordinates);
            ((ICommentProcessor) processor).setCurrentRunCoordinates(runCoordinates);
            ((ICommentProcessor) processor).setCurrentCommentWrapper(commentWrapper);
        }

        try {
            expressionResolver.resolveExpression(commentString, expressionContext);
            comments.remove(comment.getId()); // guarantee one-time processing
            logger.debug(
                    String.format("Comment '%s' has been successfully processed by a comment processor.",
                            commentString));
            return Optional.of(commentWrapper);
        } catch (SpelEvaluationException | SpelParseException e) {
            if (configuration.isFailOnUnresolvedExpression()) {
                throw new UnresolvedExpressionException(commentString, e);
            } else {
                logger.warn(String.format(
                        "Skipping comment expression '%s' because it can not be resolved by any comment processor. Reason: %s. Set log level to TRACE to view Stacktrace.",
                        commentString, e.getMessage()));
                logger.trace("Reason for skipping comment: ", e);
            }
        }
        return Optional.empty();
    }

    public void reset() {
        for (Object processor : configuration.getCommentProcessors().values()) {
            ((ICommentProcessor) processor).reset();
        }
    }
}
