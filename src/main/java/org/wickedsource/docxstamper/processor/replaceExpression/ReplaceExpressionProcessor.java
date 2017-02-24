package org.wickedsource.docxstamper.processor.replaceExpression;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.wickedsource.docxstamper.processor.BaseCommentProcessor;
import org.wickedsource.docxstamper.util.RunUtil;

public class ReplaceExpressionProcessor extends BaseCommentProcessor implements IReplaceExpressionProcessor {

	public ReplaceExpressionProcessor() {

	}

	@Override
	public void commitChanges(WordprocessingMLPackage document) {
	}

	@Override
	public void replaceExpression(String expression) {
		if (this.getCurrentRunCoordinates() != null) {
			RunUtil.setText(this.getCurrentRunCoordinates().getRun(), expression);
		}

	}

}
