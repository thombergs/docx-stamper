package org.wickedsource.docxstamper.el;

import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

public class ExpressionResolver {

    private ExpressionUtil expressionUtil = new ExpressionUtil();

    public Object resolveExpression(String expressionString, Object contextRoot) {
        if (expressionString.startsWith("${") && expressionString.endsWith("}")) {
            expressionString = expressionUtil.stripExpression(expressionString);
        }
        ExpressionParser parser = new SpelExpressionParser();
        StandardEvaluationContext evaluationContext = new StandardEvaluationContext(contextRoot);
        Expression expression = parser.parseExpression(expressionString);
        return expression.getValue(evaluationContext); // TODO: mit Image und Date usw. umgehen
    }
}
