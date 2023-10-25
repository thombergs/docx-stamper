package org.wickedsource.docxstamper.el;

import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * Resolves expressions against a given context object. Expressions can be either SpEL expressions or simple property
 * expressions.
 *
 * @author joseph
 * @version $Id: $Id
 */
public class ExpressionResolver {
    public static final Matcher DEFAULT_MATCHER = new Matcher("${", "}");
    public static final Matcher SECONDARY_MATCHER = new Matcher("#{", "}");
    private final ExpressionParser parser;
    private final StandardEvaluationContext evaluationContext;

    /**
     * Creates a new ExpressionResolver with the given SpEL parser configuration.
     *
     * @param spelParserConfiguration   the configuration for the SpEL parser.
     * @param standardEvaluationContext a {@link org.springframework.expression.spel.support.StandardEvaluationContext} object
     */
    public ExpressionResolver(
            StandardEvaluationContext standardEvaluationContext,
            SpelParserConfiguration spelParserConfiguration
    ) {
        this.parser = new SpelExpressionParser(spelParserConfiguration);
        this.evaluationContext = standardEvaluationContext;
    }

    public static String cleanExpression(String expression) {
        if (DEFAULT_MATCHER.match(expression))
            return DEFAULT_MATCHER.strip(expression);
        if (SECONDARY_MATCHER.match(expression))
            return SECONDARY_MATCHER.strip(expression);
        return expression;
    }

    /**
     * Resolves the given expression against the given context object.
     *
     * @param expression  the expression to resolve.
     * @param contextRoot the context object against which to resolve the expression.
     * @return the result of the expression evaluation.
     */
    public Object resolveExpression(String expression, Object contextRoot) {
        expression = cleanExpression(expression);
        evaluationContext.setRootObject(contextRoot);
        return parser.parseExpression(expression)
                .getValue(evaluationContext);
    }
}
