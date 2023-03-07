package org.wickedsource.docxstamper.processor;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Comments;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.SpelParseException;
import org.springframework.lang.NonNull;
import org.wickedsource.docxstamper.DocxStamperConfiguration;
import org.wickedsource.docxstamper.api.UnresolvedExpressionException;
import org.wickedsource.docxstamper.api.commentprocessor.ICommentProcessor;
import org.wickedsource.docxstamper.el.ExpressionResolver;
import org.wickedsource.docxstamper.replace.PlaceholderReplacer;
import org.wickedsource.docxstamper.util.CommentUtil;
import org.wickedsource.docxstamper.util.CommentWrapper;
import org.wickedsource.docxstamper.util.ParagraphWrapper;
import org.wickedsource.docxstamper.util.walk.BaseCoordinatesWalker;
import org.wickedsource.docxstamper.util.walk.CoordinatesWalker;

import java.math.BigInteger;
import java.util.*;

import static org.wickedsource.docxstamper.el.ExpressionUtil.findProcessorExpressions;
import static org.wickedsource.docxstamper.el.ExpressionUtil.stripExpression;

/**
 * Allows registration of ICommentProcessor objects. Each registered
 * ICommentProcessor must implement an interface which has to be specified at
 * registration time. Provides several getter methods to access the registered
 * ICommentProcessors.
 */
public class CommentProcessorRegistry {
	private final Logger logger = LoggerFactory.getLogger(CommentProcessorRegistry.class);
	private final DocxStamperConfiguration configuration;
	private final PlaceholderReplacer placeholderReplacer;
	private ExpressionResolver expressionResolver;

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
			protected void onParagraph(P paragraph) {
				runProcessorsOnParagraphComment(document, comments, expressionContext, paragraph)
						.ifPresent(proceedComments::add);
				runProcessorsOnInlineContent(expressionContext, paragraph);
			}

			@Override
			protected void onRun(R run, P paragraph) {
				runProcessorsOnRunComment(document, comments, expressionContext, paragraph, run)
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
	 * Takes the first comment on the specified paragraph and tries to evaluate
	 * the string within the comment against all registered
	 * {@link ICommentProcessor}s.
	 *
	 * @param document          the word document.
	 * @param comments          the comments within the document.
	 * @param expressionContext the context root object
	 * @param paragraph         the paragraph whose comments to evaluate.
	 * @param <T>               the type of the context root object.
	 */
	private <T> Optional<CommentWrapper> runProcessorsOnParagraphComment(
			WordprocessingMLPackage document,
			Map<BigInteger, CommentWrapper> comments,
			T expressionContext,
			P paragraph
	) {
		return CommentUtil.getCommentFor(paragraph, document)
						  .flatMap(c -> this.runCommentProcessors(comments,
																  expressionContext,
																  c,
																  paragraph,
																  null,
																  document));
	}

	/**
	 * Finds all processor expressions within the specified paragraph and tries
	 * to evaluate it against all registered {@link ICommentProcessor}s.
	 *
	 * @param expressionContext a builder for a proxy around the context root object to customize its interface
	 * @param paragraph         the paragraph to process.
	 * @param <T>               type of the context root object
	 */
	private <T> void runProcessorsOnInlineContent(
			T expressionContext,
			P paragraph
	) {
		ParagraphWrapper paragraphWrapper = new ParagraphWrapper(paragraph);
		List<String> processorExpressions = findProcessorExpressions(paragraphWrapper.getText());

		for (String processorExpression : processorExpressions) {
			String strippedExpression = stripExpression(processorExpression);

			for (final Object processor : configuration.getCommentProcessors().values()) {
				((ICommentProcessor) processor).setParagraph(paragraph);
			}

			try {
				expressionResolver.resolveExpression(strippedExpression, expressionContext);
				placeholderReplacer.replace(paragraphWrapper, processorExpression, "");
				logger.debug("Processor expression '{}' has been successfully processed by a comment processor.",
							 processorExpression);
			} catch (SpelEvaluationException | SpelParseException e) {
				if (configuration.isFailOnUnresolvedExpression()) {
					throw new UnresolvedExpressionException(strippedExpression, e);
				} else {
					logger.warn(String.format(
							"Skipping processor expression '%s' because it can not be resolved by any comment processor. Reason: %s. Set log level to TRACE to view Stacktrace.",
							processorExpression,
							e.getMessage()));
					logger.trace("Reason for skipping processor expression: ", e);
				}
			}
		}
	}

	private <T> Optional<CommentWrapper> runProcessorsOnRunComment(
			WordprocessingMLPackage document,
			Map<BigInteger, CommentWrapper> comments,
			T expressionContext,
			P paragraph,
			R run
	) {
		var comment = CommentUtil.getCommentAround(run, document);
		return comment.flatMap(c -> runCommentProcessors(comments, expressionContext, c, paragraph, run, document));
	}

	private <T> Optional<CommentWrapper> runCommentProcessors(
			Map<BigInteger, CommentWrapper> comments,
			T expressionContext,
			@NonNull Comments.Comment comment,
			P paragraph,
			R run,
			WordprocessingMLPackage document
	) {
		CommentWrapper commentWrapper = comments.get(comment.getId());

		if (Objects.isNull(commentWrapper)) {
			// no comment to process
			return Optional.empty();
		}

		String commentString = CommentUtil.getCommentString(comment);

		for (final Object processor : configuration.getCommentProcessors().values()) {
			((ICommentProcessor) processor).setParagraph(paragraph);
			((ICommentProcessor) processor).setCurrentRun(run);
			((ICommentProcessor) processor).setCurrentCommentWrapper(commentWrapper);
			((ICommentProcessor) processor).setDocument(document);
		}

		try {
			expressionResolver.resolveExpression(commentString, expressionContext);
			comments.remove(comment.getId());
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
						commentString,
						e.getMessage()));
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
