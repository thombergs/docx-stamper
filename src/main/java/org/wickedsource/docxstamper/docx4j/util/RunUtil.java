package org.wickedsource.docxstamper.docx4j.util;

import org.docx4j.jaxb.Context;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.R;
import org.docx4j.wml.Text;

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
                    result += ((Text) element.getValue()).getValue();
                }
            } else if (content instanceof Text) {
                result += ((Text) content).getValue();
            }
        }
        return result;
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
        textObj.setValue(text);
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
}
