package org.wickedsource.docxstamper.processor;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.wickedsource.docxstamper.api.commentprocessor.ICommentProcessor;
import org.wickedsource.docxstamper.replace.PlaceholderReplacer;
import org.wickedsource.docxstamper.util.CommentWrapper;

import java.util.Objects;

public abstract class BaseCommentProcessor
		implements ICommentProcessor {
	protected final PlaceholderReplacer placeholderReplacer;
	private P paragraph;
	private R run;
	private CommentWrapper currentCommentWrapper;
	private WordprocessingMLPackage document;

	public BaseCommentProcessor(PlaceholderReplacer placeholderReplacer) {
		this.placeholderReplacer = placeholderReplacer;
	}

	public R getCurrentRun() {
		return run;
	}

	@Override
	public void setCurrentRun(R run) {
		this.run = run;
	}

	public P getParagraph() {
		return paragraph;
	}

	@Override
	public void setParagraph(P paragraph) {
		this.paragraph = paragraph;
	}

	public CommentWrapper getCurrentCommentWrapper() {
		return currentCommentWrapper;
	}

	@Override
	public void setCurrentCommentWrapper(CommentWrapper currentCommentWrapper) {
		Objects.requireNonNull(currentCommentWrapper.getCommentRangeStart());
		Objects.requireNonNull(currentCommentWrapper.getCommentRangeEnd());
		this.currentCommentWrapper = currentCommentWrapper;
	}

	public WordprocessingMLPackage getDocument() {
		return document;
	}

	@Override
	public void setDocument(WordprocessingMLPackage document) {
		this.document = document;
	}
}
