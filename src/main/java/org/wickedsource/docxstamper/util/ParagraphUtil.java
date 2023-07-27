package org.wickedsource.docxstamper.util;

import org.docx4j.jaxb.Context;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.wickedsource.docxstamper.api.DocxStamperException;

/**
 * Utility class for creating paragraphs.
 *
 * @author joseph
 * @version $Id: $Id
 */
public class ParagraphUtil {

    private ParagraphUtil() {
        throw new DocxStamperException("Utility class shouldn't be instantiated");
    }

	private static final ObjectFactory objectFactory = Context.getWmlObjectFactory();

	/**
	 * Creates a new paragraph.
	 *
	 * @param texts the text of this paragraph. If more than one text is specified each text will be placed within its own Run.
	 * @return a new paragraph containing the given text.
	 */
	public static P create(String... texts) {
		P p = objectFactory.createP();
		for (String text : texts) {
			R r = RunUtil.create(text, p);
			p.getContent().add(r);
		}
		return p;
	}
}
