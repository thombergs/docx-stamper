package io.reflectoring.docxstamper.processor;

import io.reflectoring.docxstamper.util.CommentWrapper;
import io.reflectoring.docxstamper.api.commentprocessor.ICommentProcessor;
import io.reflectoring.docxstamper.api.coordinates.ParagraphCoordinates;
import io.reflectoring.docxstamper.api.coordinates.RunCoordinates;
import io.reflectoring.docxstamper.proxy.ProxyBuilder;

import java.util.Objects;

public abstract class BaseCommentProcessor implements ICommentProcessor {

	private ParagraphCoordinates currentParagraphCoordinates;

	private RunCoordinates currentRunCoordinates;

	private CommentWrapper currentCommentWrapper;

	private ProxyBuilder proxyBuilder;

	public RunCoordinates getCurrentRunCoordinates() {
		return currentRunCoordinates;
	}

	@Override
	public void setCurrentRunCoordinates(RunCoordinates currentRunCoordinates) {
		this.currentRunCoordinates = currentRunCoordinates;
	}

	@Override
	public void setCurrentParagraphCoordinates(ParagraphCoordinates coordinates) {
		this.currentParagraphCoordinates = coordinates;
	}

	public ParagraphCoordinates getCurrentParagraphCoordinates() {
		return currentParagraphCoordinates;
	}

	@Override
	public void setCurrentCommentWrapper(CommentWrapper currentCommentWrapper) {
		Objects.requireNonNull(currentCommentWrapper.getCommentRangeStart());
		Objects.requireNonNull(currentCommentWrapper.getCommentRangeEnd());
		this.currentCommentWrapper = currentCommentWrapper;
	}

	public ProxyBuilder getProxyBuilder() {
		return proxyBuilder;
	}

	@Override
	public void setProxyBuilder(ProxyBuilder proxyBuilder) {
		this.proxyBuilder = proxyBuilder;
	}

	public CommentWrapper getCurrentCommentWrapper() {
		return currentCommentWrapper;
	}
}
