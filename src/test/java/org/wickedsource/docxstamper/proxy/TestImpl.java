package org.wickedsource.docxstamper.proxy;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.wickedsource.docxstamper.api.commentprocessor.ICommentProcessor;
import org.wickedsource.docxstamper.api.coordinates.ParagraphCoordinates;
import org.wickedsource.docxstamper.api.coordinates.RunCoordinates;
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
	public void setCurrentParagraphCoordinates(ParagraphCoordinates coordinates) {

	}

	@Override
	public void setCurrentRunCoordinates(RunCoordinates coordinates) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setCurrentCommentWrapper(CommentWrapper commentWrapper) {

	}

	@Override
	public void setProxyBuilder(ProxyBuilder proxyBuilder) {

	}

	@Override
	public void reset() {

	}

}
