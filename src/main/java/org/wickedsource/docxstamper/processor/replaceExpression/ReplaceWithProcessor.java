package org.wickedsource.docxstamper.processor.replaceExpression;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.wickedsource.docxstamper.processor.BaseCommentProcessor;
import org.wickedsource.docxstamper.util.RunUtil;

public class ReplaceWithProcessor extends BaseCommentProcessor
		implements IReplaceWithProcessor {

	public ReplaceWithProcessor() {

	}

	@Override
	public void commitChanges(WordprocessingMLPackage document) {
	}

	@Override
	public void reset() {
		// nothing to rest
	}

	@Override
	public void replaceWordWith(String expression) {
		if (this.getCurrentRunCoordinates() != null) {
			RunUtil.setText(this.getCurrentRunCoordinates().getRun(), expression);
		}

	}

}
