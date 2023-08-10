package io.reflectoring.docxstamper.proxy;

import io.reflectoring.docxstamper.api.commentprocessor.ICommentProcessor;
import io.reflectoring.docxstamper.api.coordinates.ParagraphCoordinates;
import io.reflectoring.docxstamper.api.coordinates.RunCoordinates;
import io.reflectoring.docxstamper.util.CommentWrapper;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

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
