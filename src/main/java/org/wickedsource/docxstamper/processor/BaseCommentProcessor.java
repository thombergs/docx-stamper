package org.wickedsource.docxstamper.processor;

import lombok.Getter;
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

	@Getter
	private P paragraph;
	@Getter
	private R currentRun;
	@Getter
	private CommentWrapper currentCommentWrapper;
	@Getter
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

	/** {@inheritDoc}
	 @deprecated the document is passed to the processor through the commitChange method now,
	  * and will probably pe passed through the constructor in the future
	 */
	@Deprecated(since = "1.6.5", forRemoval = true)
	@Override
	public void setDocument(WordprocessingMLPackage document) {
		this.document = document;
	}
}
