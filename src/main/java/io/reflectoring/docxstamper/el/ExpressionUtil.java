package io.reflectoring.docxstamper.el;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExpressionUtil {

    /**
     * Finds all variable expressions in a text and returns them as list. Example expression: "${myObject.property}".
     *
     * @param text the text to find expressions in.
     * @return a list of expressions (including the starting "${" and trailing "}").
     */
    public List<String> findVariableExpressions(String text) {
        return findExpressions(text, "\\$\\{.*?\\}");
    }

    /**
     * Finds all processor expressions in a text and returns them as list. Example expression: "#{myObject.property}".
     *
     * @param text the text to find expressions in.
     * @return a list of expressions (including the starting "#{" and trailing "}").
     */
    public List<String> findProcessorExpressions(String text) {
        return findExpressions(text, "\\#\\{.*?\\}");
    }

    private List<String> findExpressions(String text, String expressionPattern) {
        List<String> matches = new ArrayList<>();
        if ("".equals(text) || text == null) {
            return matches;
        }
        Pattern pattern = Pattern.compile(expressionPattern);
        Matcher matcher = pattern.matcher(text);
        int index = 0;
        while (matcher.find(index)) {
            String match = matcher.group();
            matches.add(match);
            index = matcher.end();
        }
        return matches;
    }

    /**
     * Strips an expression of the leading "${" or "#{" and the trailing "}".
     *
     * @param expression the expression to strip.
     * @return the expression without the leading "${" or "#{" and the trailing "}".
     */
    public String stripExpression(String expression) {
        if (expression == null) {
            throw new IllegalArgumentException("Cannot strip NULL expression!");
        }
        expression = expression.replaceAll("^\\$\\{", "").replaceAll("\\}$", "");
        expression = expression.replaceAll("^#\\{", "").replaceAll("\\}$", "");
        return expression;
    }

}
