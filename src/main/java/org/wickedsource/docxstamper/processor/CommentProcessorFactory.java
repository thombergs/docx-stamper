package org.wickedsource.docxstamper.processor;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.wickedsource.docxstamper.DocxStamper;
import org.wickedsource.docxstamper.DocxStamperConfiguration;
import org.wickedsource.docxstamper.api.commentprocessor.ICommentProcessor;
import org.wickedsource.docxstamper.processor.displayif.DisplayIfProcessor;
import org.wickedsource.docxstamper.processor.repeat.ParagraphRepeatProcessor;
import org.wickedsource.docxstamper.processor.repeat.RepeatDocPartProcessor;
import org.wickedsource.docxstamper.processor.repeat.RepeatProcessor;
import org.wickedsource.docxstamper.processor.replaceExpression.ReplaceWithProcessor;
import org.wickedsource.docxstamper.processor.table.TableResolver;
import org.wickedsource.docxstamper.replace.PlaceholderReplacer;
import pro.verron.docxstamper.OpcStamper;

/**
 * Factory class to create the correct comment processor for a given comment.
 *
 * @author joseph
 * @version $Id: $Id
 */
public class CommentProcessorFactory {
	private final DocxStamperConfiguration configuration;

	/**
	 * Creates a new CommentProcessorFactory.
	 *
	 * @param configuration the configuration to use for the created processors.
	 */
	public CommentProcessorFactory(DocxStamperConfiguration configuration) {
		this.configuration = configuration;
	}

	/**
	 * Creates a new CommentProcessorFactory with default configuration.
	 *
	 * @param pr a {@link org.wickedsource.docxstamper.replace.PlaceholderReplacer} object
	 * @return a {@link org.wickedsource.docxstamper.api.commentprocessor.ICommentProcessor} object
	 */
	public ICommentProcessor repeatParagraph(PlaceholderReplacer pr) {
		return configuration
				.nullReplacementValue()
				.map(nullReplacementValue -> ParagraphRepeatProcessor.newInstance(pr, nullReplacementValue))
				.orElseGet(() -> ParagraphRepeatProcessor.newInstance(pr));
	}

	/**
	 * Creates a new CommentProcessorFactory with default configuration.
	 *
	 * @param pr a {@link org.wickedsource.docxstamper.replace.PlaceholderReplacer} object
	 * @return a {@link org.wickedsource.docxstamper.api.commentprocessor.ICommentProcessor} object
	 */
	public ICommentProcessor repeatDocPart(PlaceholderReplacer pr) {
		return configuration
				.nullReplacementValue()
				.map(nullReplacementValue -> RepeatDocPartProcessor.newInstance(pr, getStamper(), nullReplacementValue))
				.orElseGet(() -> RepeatDocPartProcessor.newInstance(pr, getStamper()));
	}

	private OpcStamper<WordprocessingMLPackage> getStamper() {
		return (template, context, output) -> new DocxStamper<>(configuration).stamp(template, context, output);
	}

	/**
	 * Creates a new CommentProcessorFactory with default configuration.
	 *
	 * @param pr a {@link org.wickedsource.docxstamper.replace.PlaceholderReplacer} object
	 * @return a {@link org.wickedsource.docxstamper.api.commentprocessor.ICommentProcessor} object
	 */
	public ICommentProcessor repeat(PlaceholderReplacer pr) {
		return configuration
				.nullReplacementValue()
				.map(nullReplacementValue -> RepeatProcessor.newInstanceWithNullReplacement(pr))
				.orElseGet(() -> RepeatProcessor.newInstance(pr));
	}

	/**
	 * Creates a new CommentProcessorFactory with default configuration.
	 *
	 * @param pr a {@link org.wickedsource.docxstamper.replace.PlaceholderReplacer} object
	 * @return a {@link org.wickedsource.docxstamper.api.commentprocessor.ICommentProcessor} object
	 */
	public ICommentProcessor tableResolver(PlaceholderReplacer pr) {
		return configuration
				.nullReplacementValue()
				.map(nullReplacementValue -> TableResolver.newInstance(pr, nullReplacementValue))
				.orElseGet(() -> TableResolver.newInstance(pr));
	}

	/**
	 * Creates a new CommentProcessorFactory with default configuration.
	 *
	 * @param pr a {@link org.wickedsource.docxstamper.replace.PlaceholderReplacer} object
	 * @return a {@link org.wickedsource.docxstamper.api.commentprocessor.ICommentProcessor} object
	 */
	public ICommentProcessor displayIf(PlaceholderReplacer pr) {
		return DisplayIfProcessor.newInstance(pr);
	}

	/**
	 * Creates a new CommentProcessorFactory with default configuration.
	 *
	 * @param pr a {@link org.wickedsource.docxstamper.replace.PlaceholderReplacer} object
	 * @return a {@link org.wickedsource.docxstamper.api.commentprocessor.ICommentProcessor} object
	 */
	public ICommentProcessor replaceWith(PlaceholderReplacer pr) {
		return configuration
				.nullReplacementValue()
				.map(nullReplacementValue -> ReplaceWithProcessor.newInstance(pr, nullReplacementValue))
				.orElseGet(() -> ReplaceWithProcessor.newInstance(pr));
	}
}
