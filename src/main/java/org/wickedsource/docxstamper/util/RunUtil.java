package org.wickedsource.docxstamper.util;

import jakarta.xml.bind.JAXBElement;
import org.docx4j.jaxb.Context;
import org.docx4j.model.styles.StyleUtil;
import org.docx4j.wml.*;
import org.wickedsource.docxstamper.api.DocxStamperException;

import java.util.Objects;


public class RunUtil {

	public static final String PRESERVE = "preserve";
	private static final ObjectFactory factory = Context.getWmlObjectFactory();

	private RunUtil() {
		throw new DocxStamperException("Utility class shouldn't be instantiated");
	}

	/**
	 * Returns the text string of a run.
	 *
	 * @param run the run whose text to get.
	 * @return String representation of the run.
	 */
	public static String getText(R run) {
		StringBuilder result = new StringBuilder();
		for (Object content : run.getContent()) {
			if (content instanceof JAXBElement) {
				result.append(getText((JAXBElement<?>) content));
			} else if (content instanceof Text) {
				result.append(getText((Text) content));
			}
		}
		return result.toString();
	}

	private static CharSequence getText(JAXBElement<?> content) {
		Object elementValue = content.getValue();
		if (elementValue instanceof Text)
			return getText((Text) elementValue);
		if (elementValue instanceof R.Tab)
			return "\t";
		return "";
	}

	private static CharSequence getText(Text text) {
		String value = text.getValue();
		String space = text.getSpace();
		return Objects.equals(space, PRESERVE)
				? value // keeps spaces if spaces are to be preserved (LibreOffice seems to ignore the "space" property)
				: value.trim(); // trimming value if spaces are not to be preserved (simulates behavior of Word;)
	}

	/**
	 * Creates a new run with the given object as content.
	 *
	 * @param content the content of the run.
	 * @return the newly created run.
	 */
	public static R create(Object content) {
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
		textObj.setSpace(PRESERVE);
		textObj.setValue(text);
		textObj.setSpace(PRESERVE); // make the text preserve spaces
		run.getContent().add(textObj);
	}

	public static R createRun(String text) {
		R run = factory.createR();
		setText(run, text);
		return run;
	}
}
