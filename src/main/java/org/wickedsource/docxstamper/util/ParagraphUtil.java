package org.wickedsource.docxstamper.util;

import org.docx4j.TraversalUtil;
import org.docx4j.finders.ClassFinder;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.CTTxbxContent;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.R;

import java.util.ArrayList;
import java.util.List;

public class ParagraphUtil {

    private static final ObjectFactory objectFactory = Context.getWmlObjectFactory();

    private ParagraphUtil() {

    }

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

    /**
     * Finds all Paragraphs in a Document which are in a TextBox
     * 
     *  
     */
    public static List<Object> getAllTextBoxes(WordprocessingMLPackage document) {
        ClassFinder finder = new ClassFinder(P.class); // docx4j class
        //necessary even if not used
        new TraversalUtil(document.getMainDocumentPart().getContent(),finder); // docx4j class
        ArrayList<Object> result = new ArrayList<>(finder.results.size());
        for (Object o : finder.results) {
            if (o instanceof P && ((P) o).getParent() instanceof CTTxbxContent) {
                result.add(o);
            }
        }
        return result;
    }

}
