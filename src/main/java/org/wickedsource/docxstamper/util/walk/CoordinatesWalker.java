package org.wickedsource.docxstamper.util.walk;


import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.wickedsource.docxstamper.util.DocumentUtil;

public abstract class CoordinatesWalker {
	public void walk(WordprocessingMLPackage document) {
		DocumentUtil.streamParagraphs(document).forEach(this::walk);
	}

	private void walk(P paragraph) {
		paragraph.getContent().stream()
				 .map(XmlUtils::unwrap)
				 .filter(element -> element instanceof R)
				 .map(R.class::cast)
				 .forEach(run -> onRun(run, paragraph));
		// we run the paragraph afterward so that the comments inside work before the whole paragraph comments
		onParagraph(paragraph);
	}

	protected abstract void onRun(R run, P paragraph);

	protected abstract void onParagraph(P paragraph);
}
