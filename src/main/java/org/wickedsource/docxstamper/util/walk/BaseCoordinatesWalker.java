package org.wickedsource.docxstamper.util.walk;

import org.docx4j.wml.P;
import org.docx4j.wml.R;

/**
 * A {@link org.wickedsource.docxstamper.util.walk.CoordinatesWalker} that does nothing in the {@link #onRun(R, P)} and {@link #onParagraph(P)} methods.
 *
 * @author joseph
 * @version $Id: $Id
 */
public abstract class BaseCoordinatesWalker extends CoordinatesWalker {

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onRun(R run, P paragraph) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onParagraph(P paragraph) {
	}
}
