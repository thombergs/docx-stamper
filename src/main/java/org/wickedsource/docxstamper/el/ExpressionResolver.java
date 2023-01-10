package org.wickedsource.docxstamper.el;

import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.wickedsource.docxstamper.DocxStamperConfiguration;

public class ExpressionResolver {

    private static final ExpressionUtil expressionUtil = new ExpressionUtil();
    private final DocxStamperConfiguration configuration;

    public ExpressionResolver(DocxStamperConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * Runs the given expression against the given context object and returns the result of the evaluated expression.
     *
     * @param expressionString the expression to evaluate.
     * @param contextRoot      the context object against which the expression is evaluated.
     * @return the result of the evaluated expression.
     */
    public Object resolveExpression(String expressionString, Object contextRoot) {
        if ((expressionString.startsWith("${") || expressionString.startsWith("#{")) && expressionString.endsWith("}")) {
            expressionString = expressionUtil.stripExpression(expressionString);
        }
        ExpressionParser parser = new SpelExpressionParser();
        StandardEvaluationContext evaluationContext = new StandardEvaluationContext(contextRoot);
        evaluationContext.addMethodResolver(new StandardMethodResolver(configuration));
        configuration.getEvaluationContextConfigurer().configureEvaluationContext(evaluationContext);
        Expression expression = parser.parseExpression(expressionString);
        return expression.getValue(evaluationContext);
    }

}
