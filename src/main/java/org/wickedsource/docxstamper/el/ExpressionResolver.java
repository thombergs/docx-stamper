package org.wickedsource.docxstamper.el;

import org.springframework.expression.EvaluationException;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.wickedsource.docxstamper.api.EvaluationContextConfigurer;

import java.lang.reflect.Field;

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
     * @param elemObject
     * @return the result of the evaluated expression.
     */
    public Object resolveExpression(String expressionString, Object contextRoot, ElemObject elemObject) {
        if ((expressionString.startsWith("${") || expressionString.startsWith("#{")) && expressionString.endsWith("}")) {
            expressionString = expressionUtil.stripExpression(expressionString);
        }
        Object value = null;
        try {
            ExpressionParser parser = new SpelExpressionParser();
            StandardEvaluationContext evaluationContext = new StandardEvaluationContext(contextRoot);
            evaluationContextConfigurer.configureEvaluationContext(evaluationContext);
            Expression expression = parser.parseExpression(expressionString);
            return expression.getValue(evaluationContext);
        } catch (EvaluationException e) {
            try {
                Class<? extends ElemObject> elemObjectClass = elemObject.getClass();
                Field field = elemObjectClass.getDeclaredField(expressionString);
                field.setAccessible(true);
                Class<?> type = field.getType();
                if (type.equals(Integer.class)) {
                    value = field.get(elemObject);
                } else if (type.equals(boolean.class)) {
                    value = field.getBoolean(elemObject);
                }
            } catch (IllegalAccessException | NoSuchFieldException ex) {
                ex.printStackTrace();
            }
            if (value == null) {
                throw e;
            }
        }
        return value;
    }

}
