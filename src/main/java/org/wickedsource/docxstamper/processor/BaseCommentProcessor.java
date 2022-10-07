package org.wickedsource.docxstamper.processor;

import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.wickedsource.docxstamper.api.commentprocessor.ICommentProcessor;
import org.wickedsource.docxstamper.util.CommentWrapper;

import java.util.Objects;

public abstract class BaseCommentProcessor implements ICommentProcessor {

	private P paragraph;

	private R currentRun;

	private CommentWrapper currentCommentWrapper;

	public R getCurrentRun() {
		return currentRun;
	}

	@Override
	public void setCurrentRun(R currentRun) {
		this.currentRun = currentRun;
	}

	@Override
	public void setParagraph(P paragraph) {
		this.paragraph = paragraph;
	}

	public P getParagraph() {
		return paragraph;
	}
	@Override
	public void setCurrentCommentWrapper(CommentWrapper currentCommentWrapper) {
		Objects.requireNonNull(currentCommentWrapper.getCommentRangeStart());
		Objects.requireNonNull(currentCommentWrapper.getCommentRangeEnd());
		this.currentCommentWrapper = currentCommentWrapper;
	}

	public CommentWrapper getCurrentCommentWrapper() {
		return currentCommentWrapper;
	}
}
