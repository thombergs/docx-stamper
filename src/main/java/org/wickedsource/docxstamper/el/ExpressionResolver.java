package org.wickedsource.docxstamper.el;

import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.wickedsource.docxstamper.api.EvaluationContextConfigurer;

import java.util.Map;

import static org.wickedsource.docxstamper.el.ExpressionUtil.stripExpression;

public class ExpressionResolver {
	private final boolean failOnUnresolvedExpression;
	private final Map<Class<?>, Object> commentProcessors;
	private final Map<Class<?>, Object> expressionFunctions;
	private final EvaluationContextConfigurer evaluationContextConfigurer;

	public ExpressionResolver(
			boolean failOnUnresolvedExpression1,
			Map<Class<?>, Object> commentProcessors1,
			Map<Class<?>, Object> expressionFunctions1,
			EvaluationContextConfigurer evaluationContextConfigurer1
	) {
		failOnUnresolvedExpression = failOnUnresolvedExpression1;
		commentProcessors = commentProcessors1;
		expressionFunctions = expressionFunctions1;
		evaluationContextConfigurer = evaluationContextConfigurer1;
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
			expressionString = stripExpression(expressionString);
		}
		StandardEvaluationContext evaluationContext = new StandardEvaluationContext(contextRoot);
		StandardMethodResolver methodResolver = new StandardMethodResolver(
				failOnUnresolvedExpression,
				commentProcessors,
				expressionFunctions);
		evaluationContext.addMethodResolver(methodResolver);
		evaluationContextConfigurer.configureEvaluationContext(evaluationContext);
		ExpressionParser parser = new SpelExpressionParser();
		Expression expression = parser.parseExpression(expressionString);
		return expression.getValue(evaluationContext);
	}
}
