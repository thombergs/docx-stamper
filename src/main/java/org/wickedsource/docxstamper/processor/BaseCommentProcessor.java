package org.wickedsource.docxstamper.processor;

import org.wickedsource.docxstamper.api.commentprocessor.ICommentProcessor;
import org.wickedsource.docxstamper.api.coordinates.ParagraphCoordinates;
import org.wickedsource.docxstamper.api.coordinates.RunCoordinates;

public abstract class BaseCommentProcessor implements ICommentProcessor {

	private ParagraphCoordinates currentParagraphCoordinates;

	private RunCoordinates currentRunCoordinates;

	public RunCoordinates getCurrentRunCoordinates() {
		return currentRunCoordinates;
	}

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

}
