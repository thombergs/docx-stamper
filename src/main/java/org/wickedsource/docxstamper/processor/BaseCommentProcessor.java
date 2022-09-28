package org.wickedsource.docxstamper.processor;

import org.docx4j.wml.P;
import org.wickedsource.docxstamper.api.commentprocessor.ICommentProcessor;
import org.wickedsource.docxstamper.api.coordinates.ParagraphCoordinates;
import org.wickedsource.docxstamper.api.coordinates.RunCoordinates;
import org.wickedsource.docxstamper.util.CommentWrapper;

import java.util.Objects;

public abstract class BaseCommentProcessor implements ICommentProcessor {

	private P paragraph;

	private RunCoordinates currentRunCoordinates;

	private CommentWrapper currentCommentWrapper;

	public RunCoordinates getCurrentRunCoordinates() {
		return currentRunCoordinates;
	}

	@Override
	public void setCurrentRunCoordinates(RunCoordinates currentRunCoordinates) {
		this.currentRunCoordinates = currentRunCoordinates;
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
