package org.wickedsource.docxstamper.util.walk;

import org.docx4j.wml.P;
import org.docx4j.wml.R;

public abstract class BaseCoordinatesWalker extends CoordinatesWalker {

	@Override
	protected void onRun(R run, P paragraph) {
	}

	@Override
	protected void onParagraph(P paragraph) {
	}
}
