package org.wickedsource.docxstamper.el;

import org.springframework.lang.NonNull;
import org.wickedsource.docxstamper.api.DocxStamperException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Collections.emptyList;

/**
 * Utility class for working with expressions in a text.
 *
 * @author joseph
 * @version $Id: $Id
 */
public class ExpressionUtil {
	private static final Pattern VARIABLE_EXPRESSION_PATTERN = Pattern.compile("\\$\\{(.*?)}");
	private static final Pattern PROCESSOR_EXPRESSION_PATTERN = Pattern.compile("#\\{(.*?)}");

	private ExpressionUtil() {
		throw new DocxStamperException("Utility classes should not be instantiated!");
	}

	/**
	 * Finds all variable expressions in a text and returns them as list. Example expression: "${myObject.property}".
	 *
	 * @param text the text to find expressions in.
	 * @return a list of expressions (including the starting "${" and trailing "}").
	 */
	public static List<String> findVariableExpressions(@NonNull String text) {
		return findExpressions(text, VARIABLE_EXPRESSION_PATTERN);
	}

	private static List<String> findExpressions(@NonNull String text, Pattern pattern) {
		if (text.isEmpty())
			return emptyList();
		Matcher matcher = pattern.matcher(text);
		int index = 0;
		List<String> matches = new ArrayList<>();
		while (matcher.find(index)) {
			String match = matcher.group();
			matches.add(match);
			index = matcher.end();
		}
		return matches;
	}

	/**
	 * Finds all processor expressions in a text and returns them as list. Example expression: "#{myObject.property}".
	 *
	 * @param text the text to find expressions in.
	 * @return a list of expressions (including the starting "#{" and trailing "}").
	 */
	public static List<String> findProcessorExpressions(@NonNull String text) {
		return findExpressions(text, PROCESSOR_EXPRESSION_PATTERN);
	}


}
