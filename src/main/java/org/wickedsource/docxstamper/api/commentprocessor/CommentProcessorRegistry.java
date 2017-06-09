package org.wickedsource.docxstamper.api.commentprocessor;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Comments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.SpelParseException;
import org.wickedsource.docxstamper.api.DocxStamperException;
import org.wickedsource.docxstamper.api.UnresolvedExpressionException;
import org.wickedsource.docxstamper.el.ExpressionResolver;
import org.wickedsource.docxstamper.el.ExpressionUtil;
import org.wickedsource.docxstamper.proxy.ContextFactory;
import org.wickedsource.docxstamper.proxy.ProxyException;
import org.wickedsource.docxstamper.replace.ParagraphWrapper;
import org.wickedsource.docxstamper.replace.PlaceholderReplacer;
import org.wickedsource.docxstamper.util.CommentUtil;
import org.wickedsource.docxstamper.util.CommentWrapper;
import org.wickedsource.docxstamper.walk.coordinates.BaseCoordinatesWalker;
import org.wickedsource.docxstamper.walk.coordinates.CoordinatesWalker;
import org.wickedsource.docxstamper.walk.coordinates.ParagraphCoordinates;
import org.wickedsource.docxstamper.walk.coordinates.RunCoordinates;

/**
 * Allows registration of ICommentProcessor objects. Each registered
 * ICommentProcessor must implement an interface which has to be specified at
 * registration time. Provides several getter methods to access the registered
 * ICommentProcessors.
 */
public class CommentProcessorRegistry {

	private Logger logger = LoggerFactory.getLogger(CommentProcessorRegistry.class);

	private Map<ICommentProcessor, Class<?>> commentProcessorInterfaces = new HashMap<>();

	private List<ICommentProcessor> commentProcessors = new ArrayList<>();

	private ExpressionResolver expressionResolver = new ExpressionResolver();

	private ExpressionUtil expressionUtil = new ExpressionUtil();

	private PlaceholderReplacer placeholderReplacer;

	private boolean failOnInvalidExpression = true;

	public CommentProcessorRegistry(PlaceholderReplacer placeholderReplacer) {
		this.placeholderReplacer = placeholderReplacer;
	}

	/**
	 * Registers the specified ICommentProcessor as an implementation of the
	 * specified interface.
	 *
	 * @param interfaceClass
	 *            the Interface which is implemented by the commentProcessor.
	 * @param commentProcessor
	 *            the commentProcessor implementing the specified interface.
	 */
	public void registerCommentProcessor(Class<?> interfaceClass, ICommentProcessor commentProcessor) {
		this.commentProcessorInterfaces.put(commentProcessor, interfaceClass);
		this.commentProcessors.add(commentProcessor);
	}

	/**
	 * Lets each registered ICommentProcessor have a run on the specified docx
	 * document. At the end of the document the commit method is called for each
	 * ICommentProcessor. The ICommentProcessors are run in the order they were
	 * registered.
	 *
	 * @param document
	 *            the docx document over which to run the registered
	 *            ICommentProcessors.
	 * @param contextRoot
	 *            the context root object against which to resolve expressions
	 *            within the comments.
	 * @param <T>
	 *            type of the contextRoot object.
	 */
	public <T> void runProcessors(final WordprocessingMLPackage document, final T contextRoot) {
		final Map<BigInteger, CommentWrapper> comments = CommentUtil.getComments(document);

		CoordinatesWalker walker = new BaseCoordinatesWalker(document) {

			@Override
			protected void onParagraph(ParagraphCoordinates paragraphCoordinates) {
				runProcessorsOnParagraphComment(document, comments, contextRoot, paragraphCoordinates);
				runProcessorsOnInlineContent(contextRoot, paragraphCoordinates);
			}

			@Override
			protected CommentWrapper onRun(RunCoordinates runCoordinates, ParagraphCoordinates paragraphCoordinates) {
				return runProcessorsOnRunComment(document, comments, contextRoot, runCoordinates, paragraphCoordinates);
			}

		};
		walker.walk();

		for (ICommentProcessor processor : commentProcessors) {
			processor.commitChanges(document);
		}

	}

	/**
	 * Finds all processor expressions within the specified paragraph and tries
	 * to evaluate it against all registered {@link ICommentProcessor}s.
	 *
	 * @param contextRoot
	 *            the context root used for expression evaluation
	 * @param paragraphCoordinates
	 *            the paragraph to process.
	 * @param <T>
	 *            type of the context root object
	 */
	private <T> void runProcessorsOnInlineContent(T contextRoot, ParagraphCoordinates paragraphCoordinates) {

		ParagraphWrapper paragraph = new ParagraphWrapper(paragraphCoordinates.getParagraph());
		List<String> processorExpressions = expressionUtil.findProcessorExpressions(paragraph.getText());
		for (String processorExpression : processorExpressions) {
			String strippedExpression = expressionUtil.stripExpression(processorExpression);
			Exception latestException = null;
			boolean expressionResolvedSuccessfully = false;
			for (final ICommentProcessor processor : commentProcessors) {
				try {
					Class<?> commentProcessorInterface = commentProcessorInterfaces.get(processor);
					ContextFactory<T> contextFactory = new ContextFactory<>();
					final T contextRootProxy = contextFactory.createProxy(contextRoot, commentProcessorInterface,
							processor);
					processor.setCurrentParagraphCoordinates(paragraphCoordinates);
					expressionResolver.resolveExpression(strippedExpression, contextRootProxy);
					placeholderReplacer.replace(paragraph, processorExpression, null);
					logger.debug(String.format(
							"Processor expression '%s' has been successfully processed by comment processor %s.",
							processorExpression, processor.getClass()));
					expressionResolvedSuccessfully = true;
					continue;
				} catch (SpelEvaluationException | SpelParseException e) {
					latestException = e;
					logger.warn(String.format(
							"Skipping processor expression '%s' because it can not be resolved by comment processor %s. Reason: %s. Set log level to TRACE to view Stacktrace.",
							processorExpression, processor.getClass(), e.getMessage()));
					logger.trace("Reason for skipping processor expression: ", e);
				} catch (ProxyException e) {
					throw new DocxStamperException(
							String.format("Could not create a proxy around context root object of class %s",
									contextRoot.getClass()),
							e);
				}
			}

			// processor expression could not be processed by any processor ->
			// throw the latest exception
			if (failOnInvalidExpression && !expressionResolvedSuccessfully) {
				throw new UnresolvedExpressionException(strippedExpression, latestException);
			}
		}
	}

	/**
	 * Takes the first comment on the specified paragraph and tries to evaluate
	 * the string within the comment against all registered
	 * {@link ICommentProcessor}s.
	 *
	 * @param document
	 *            the word document.
	 * @param comments
	 *            the comments within the document.
	 * @param contextRoot
	 *            the context root against which to evaluate the expressions.
	 * @param paragraphCoordinates
	 *            the paragraph whose comments to evaluate.
	 * @param <T>
	 *            the type of the context root object.
	 */
	private <T> void runProcessorsOnParagraphComment(final WordprocessingMLPackage document,
			final Map<BigInteger, CommentWrapper> comments, T contextRoot, ParagraphCoordinates paragraphCoordinates) {
		Exception latestException = null;
		boolean expressionResolvedSuccessfully = false;
		Comments.Comment comment = CommentUtil.getCommentFor(paragraphCoordinates.getParagraph(), document);
		if (comment == null) {
			// no comment to process
			return;
		}

		String commentString = CommentUtil.getCommentString(comment);
		for (final ICommentProcessor processor : commentProcessors) {
			try {
				Class<?> commentProcessorInterface = commentProcessorInterfaces.get(processor);
				ContextFactory<T> contextFactory = new ContextFactory<>();
				final T contextRootProxy = contextFactory.createProxy(contextRoot, commentProcessorInterface,
						processor);
				processor.setCurrentParagraphCoordinates(paragraphCoordinates);
				CommentWrapper commentWrapper = comments.get(comment.getId());

				try {
					expressionResolver.resolveExpression(commentString, contextRootProxy);
					CommentUtil.deleteComment(commentWrapper);
					logger.debug(String.format("Comment '%s' has been successfully processed by comment processor %s.",
							commentString, processor.getClass()));
					// expression has been resolved, don't try other processors
					expressionResolvedSuccessfully = true;
					break;
				} catch (SpelEvaluationException | SpelParseException e) {
					latestException = e;
					logger.warn(String.format(
							"Skipping comment expression '%s' because it can not be resolved by comment processor %s. Reason: %s. Set log level to TRACE to view Stacktrace.",
							commentString, processor.getClass(), e.getMessage()));
					logger.trace("Reason for skipping comment: ", e);
				}

			} catch (ProxyException e) {
				throw new DocxStamperException(String.format(
						"Could not create a proxy around context root object of class %s", contextRoot.getClass()), e);
			}
		}

		// comment could not be processed by any processor -> throw the latest
		// exception
		if (failOnInvalidExpression && !expressionResolvedSuccessfully) {
			throw new UnresolvedExpressionException(commentString, latestException);
		}

	}

	private <T> CommentWrapper runProcessorsOnRunComment(final WordprocessingMLPackage document,
			final Map<BigInteger, CommentWrapper> comments, T contextRoot, RunCoordinates runCoordinates, ParagraphCoordinates paragraphCoordinates) {
		Exception latestException = null;
		boolean expressionResolvedSuccessfully = false;
		Comments.Comment comment = CommentUtil.getCommentAround(runCoordinates.getRun(), document);
		if (comment == null) {
			// no comment to process
			return null;
		}

		String commentString = CommentUtil.getCommentString(comment);
		for (final ICommentProcessor processor : commentProcessors) {
			try {
				Class<?> commentProcessorInterface = commentProcessorInterfaces.get(processor);
				ContextFactory<T> contextFactory = new ContextFactory<>();
				final T contextRootProxy = contextFactory.createProxy(contextRoot, commentProcessorInterface,
						processor);
				processor.setCurrentRunCoordinates(runCoordinates);
				processor.setCurrentParagraphCoordinates(paragraphCoordinates);
				CommentWrapper commentWrapper = comments.get(comment.getId());

				try {
					expressionResolver.resolveExpression(commentString, contextRootProxy);
					logger.debug(String.format("Comment '%s' has been successfully processed by comment processor %s.",
							commentString, processor.getClass()));
					// expression has been resolved, don't try other processors
					expressionResolvedSuccessfully = true;
					return commentWrapper;
				} catch (SpelEvaluationException | SpelParseException e) {
					latestException = e;
					logger.warn(String.format(
							"Skipping comment expression '%s' because it can not be resolved by comment processor %s. Reason: %s. Set log level to TRACE to view Stacktrace.",
							commentString, processor.getClass(), e.getMessage()));
					logger.trace("Reason for skipping comment: ", e);
				}

			} catch (ProxyException e) {
				throw new DocxStamperException(String.format(
						"Could not create a proxy around context root object of class %s", contextRoot.getClass()), e);
			}
		}

		// comment could not be processed by any processor -> throw the latest
		// exception
		if (failOnInvalidExpression && !expressionResolvedSuccessfully) {
			throw new UnresolvedExpressionException(commentString, latestException);
		}
		return null;

	}

	public boolean isFailOnInvalidExpression() {
		return failOnInvalidExpression;
	}

	public void setFailOnInvalidExpression(boolean failOnInvalidExpression) {
		this.failOnInvalidExpression = failOnInvalidExpression;
	}

	public void reset(){
		for(ICommentProcessor processor : commentProcessors){
			processor.reset();
		}
	}
}
