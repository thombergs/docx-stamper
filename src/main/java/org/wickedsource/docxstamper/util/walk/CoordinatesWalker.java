package org.wickedsource.docxstamper.util.walk;


import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.wickedsource.docxstamper.util.DocumentUtil;

/**
 * Walks through a document and calls the methods on each paragraph and run.
 *
 * @author joseph
 * @version $Id: $Id
 */
public abstract class CoordinatesWalker {
    /**
     * walks through the document
     *
     * @param document the document
     */
	public void walk(WordprocessingMLPackage document) {
		DocumentUtil.streamParagraphs(document).forEach(this::walk);
	}

	private void walk(P paragraph) {
		paragraph.getContent().stream()
				 .map(XmlUtils::unwrap)
				.filter(R.class::isInstance)
				 .map(R.class::cast)
				 .forEach(run -> onRun(run, paragraph));
		// we run the paragraph afterward so that the comments inside work before the whole paragraph comments
		onParagraph(paragraph);
    }

    /**
     * called for each run
     *
     * @param run       the run
     * @param paragraph the paragraph containing the run
     */
	protected abstract void onRun(R run, P paragraph);

    /**
     * called after each paragraph
     *
     * @param paragraph the paragraph
     */
	protected abstract void onParagraph(P paragraph);
}
