package org.wickedsource.docxstamper.processor;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.wickedsource.docxstamper.api.commentprocessor.ICommentProcessor;
import org.wickedsource.docxstamper.replace.PlaceholderReplacer;
import org.wickedsource.docxstamper.util.CommentWrapper;

import java.util.Objects;

/**
 * Base class for comment processors. The current run and paragraph are set by the {@link org.wickedsource.docxstamper.DocxStamper} class.
 *
 * @author joseph
 * @version $Id: $Id
 */
public abstract class BaseCommentProcessor implements ICommentProcessor {

	/**
	 * PlaceholderReplacer used to replace placeholders in the comment text.
	 */
	protected final PlaceholderReplacer placeholderReplacer;

	private P paragraph;
	private R currentRun;
	private CommentWrapper currentCommentWrapper;
	private WordprocessingMLPackage document;

	/**
	 * <p>Constructor for BaseCommentProcessor.</p>
	 *
	 * @param placeholderReplacer PlaceholderReplacer used to replace placeholders in the comment text.
	 */
	protected BaseCommentProcessor(PlaceholderReplacer placeholderReplacer) {
		this.placeholderReplacer = placeholderReplacer;
	}

	/** {@inheritDoc} */
	@Override
	public void setCurrentRun(R run) {
		this.currentRun = run;
	}

	/** {@inheritDoc} */
	@Override
	public void setParagraph(P paragraph) {
		this.paragraph = paragraph;
	}

	/** {@inheritDoc} */
	@Override
	public void setCurrentCommentWrapper(CommentWrapper currentCommentWrapper) {
		Objects.requireNonNull(currentCommentWrapper.getCommentRangeStart());
		Objects.requireNonNull(currentCommentWrapper.getCommentRangeEnd());
		this.currentCommentWrapper = currentCommentWrapper;
	}

	/**
	 * {@inheritDoc}
	 * @deprecated the document is passed to the processor through the commitChange method now,
	 * and will probably pe passed through the constructor in the future
	 */
	@Deprecated(since = "1.6.5", forRemoval = true)
	@Override
	public void setDocument(WordprocessingMLPackage document) {
		this.document = document;
	}

	/**
	 * <p>Getter for the field <code>currentCommentWrapper</code>.</p>
	 *
	 * @return a {@link org.wickedsource.docxstamper.util.CommentWrapper} object
	 */
	public CommentWrapper getCurrentCommentWrapper() {
		return currentCommentWrapper;
	}

	/**
	 * <p>Getter for the field <code>paragraph</code>.</p>
	 *
	 * @return a {@link org.docx4j.wml.P} object
	 */
	public P getParagraph() {
		return paragraph;
	}

	/**
	 * <p>Getter for the field <code>currentRun</code>.</p>
	 *
	 * @return a {@link org.docx4j.wml.R} object
	 */
	public R getCurrentRun() {
		return currentRun;
	}

	/**
	 * <p>Getter for the field <code>document</code>.</p>
	 *
	 * @return a {@link org.docx4j.openpackaging.packages.WordprocessingMLPackage} object
	 * @deprecated the document is passed to the processor through the commitChange method now
	 * and will probably pe passed through the constructor in the future
	 */
	@Deprecated(since = "1.6.5", forRemoval = true)
	public WordprocessingMLPackage getDocument() {
		return document;
	}
}
