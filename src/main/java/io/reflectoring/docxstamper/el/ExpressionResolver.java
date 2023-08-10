package io.reflectoring.docxstamper.el;

import io.reflectoring.docxstamper.api.EvaluationContextConfigurer;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

public class ExpressionResolver {

    private static final ExpressionUtil expressionUtil = new ExpressionUtil();

    private final EvaluationContextConfigurer evaluationContextConfigurer;

    public ExpressionResolver() {
        this.evaluationContextConfigurer = new NoOpEvaluationContextConfigurer();
    }

    public ExpressionResolver(EvaluationContextConfigurer evaluationContextConfigurer) {
        this.evaluationContextConfigurer = evaluationContextConfigurer;
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
        evaluationContextConfigurer.configureEvaluationContext(evaluationContext);
        Expression expression = parser.parseExpression(expressionString);
        return expression.getValue(evaluationContext);
    }

}
