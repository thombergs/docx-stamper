package io.reflectoring.docxstamper.processor.replaceExpression;

import io.reflectoring.docxstamper.processor.BaseCommentProcessor;
import io.reflectoring.docxstamper.util.RunUtil;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

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
