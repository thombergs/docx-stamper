package org.wickedsource.docxstamper.el;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

public class ExpressionResolver {

    private Logger logger = LoggerFactory.getLogger(ExpressionResolver.class);

    private ExpressionUtil expressionUtil = new ExpressionUtil();

    public Object resolveExpression(String expressionString, Object contextRoot) {
        try {
            if (expressionString.startsWith("${") && expressionString.endsWith("}")) {
                expressionString = expressionUtil.stripExpression(expressionString);
            }
            ExpressionParser parser = new SpelExpressionParser();
            StandardEvaluationContext evaluationContext = new StandardEvaluationContext(contextRoot);
            Expression expression = parser.parseExpression(expressionString);
            return expression.getValue(evaluationContext); // TODO: mit Image und Date usw. umgehen
        } catch (SpelEvaluationException e) {
            logger.warn(String.format("Expression %s could not be resolved against context root of type %s", expressionString, contextRoot.getClass()));
            return null;
        }
    }
}
