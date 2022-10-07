package org.wickedsource.docxstamper.proxy;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.wickedsource.docxstamper.api.commentprocessor.ICommentProcessor;
import org.wickedsource.docxstamper.util.CommentWrapper;

public class TestImpl implements ITestInterface, ICommentProcessor {

	@Override
	public String returnString(String string) {
		return string;
	}

	@Override
	public void commitChanges(WordprocessingMLPackage document) {

	}

	@Override
	public void setParagraph(P paragraph) {

	}

	@Override
	public void setCurrentRun(R coordinates) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setCurrentCommentWrapper(CommentWrapper commentWrapper) {

	}

	@Override
	public void reset() {

	}

}
