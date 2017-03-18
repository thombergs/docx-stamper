package org.wickedsource.docxstamper.util;

import org.docx4j.jaxb.Context;
import org.docx4j.model.styles.StyleUtil;
import org.docx4j.wml.*;

import javax.xml.bind.JAXBElement;

public class RunUtil {

    private static ObjectFactory factory = Context.getWmlObjectFactory();

    private RunUtil() {

    }

    /**
     * Returns the text string of a run.
     *
     * @param run the run whose text to get.
     * @return String representation of the run.
     */
    public static String getText(R run) {
        String result = "";
        for (Object content : run.getContent()) {
            if (content instanceof JAXBElement) {
                JAXBElement element = (JAXBElement) content;
                if (element.getValue() instanceof Text) {
                    Text textObj = (Text) element.getValue();
                    String text = textObj.getValue();
                    if (!"preserve".equals(textObj.getSpace())) {
                        // trimming text if spaces are not to be preserved (simulates behavior of Word; LibreOffice seems
                        // to ignore the "space" property and always preserves spaces)
                        text = text.trim();
                    }
                    result += text;
                }
            } else if (content instanceof Text) {
                result += ((Text) content).getValue();
            }
        }
        return result;
    }

    /**
     * Applies the style of the given paragraph to the given content object (if the content object is a Run).
     *
     * @param p   the paragraph whose style to use.
     * @param run the Run to which the style should be applied.
     */
    public static void applyParagraphStyle(P p, R run) {
        if (p.getPPr() != null && p.getPPr().getRPr() != null) {
            RPr runProperties = new RPr();
            StyleUtil.apply(p.getPPr().getRPr(), runProperties);
            run.setRPr(runProperties);
        }
    }

    /**
     * Sets the text of the given run to the given value.
     *
     * @param run  the run whose text to change.
     * @param text the text to set.
     */
    public static void setText(R run, String text) {
        run.getContent().clear();
        Text textObj = factory.createText();
        textObj.setSpace("preserve");
        textObj.setValue(text);
        textObj.setSpace("preserve"); // make the text preserve spaces
        run.getContent().add(textObj);
    }

    /**
     * Creates a new run with the specified text.
     *
     * @param text the initial text of the run.
     * @return the newly created run.
     */
    public static R create(String text) {
        R run = factory.createR();
        setText(run, text);
        return run;
    }

    /**
     * Creates a new run with the given object as content.
     * @param content the content of the run.
     * @return the newly created run.
     */
    public static R create(Object content){
        R run = factory.createR();
        run.getContent().add(content);
        return run;
    }

    /**
     * Creates a new run with the specified text and inherits the style of the parent paragraph.
     *
     * @param text            the initial text of the run.
     * @param parentParagraph the parent paragraph whose style to inherit.
     * @return the newly created run.
     */
    public static R create(String text, P parentParagraph) {
        R run = create(text);
        applyParagraphStyle(parentParagraph, run);
        return run;
    }
}
