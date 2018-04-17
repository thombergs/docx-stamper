package org.wickedsource.docxstamper.util;

import org.docx4j.jaxb.Context;
import org.docx4j.wml.Body;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.R;

public class ParagraphUtil {

    private static ObjectFactory objectFactory = Context.getWmlObjectFactory();

    private ParagraphUtil() {

    }

    /**
     * Creates a new paragraph.
     *
     * @param texts the text of this paragraph. If more than one text is specified each text will be placed within its own Run.
     * @return a new paragraph containing the given text.
     */
    public static P create(String... texts) {
        Body b = objectFactory.createBody(); 
        P p = objectFactory.createP();
        for (String text : texts) {
            R r = RunUtil.create(text, p);
            p.getContent().add(r);
        }
        b.getContent().add(p);
        p.setParent(b);
        return p;
    }


}
