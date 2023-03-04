package org.wickedsource.docxstamper.el;

import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.MethodExecutor;
import org.springframework.expression.TypedValue;
import org.springframework.lang.NonNull;

import java.lang.reflect.InvocationTargetException;

public class StandardMethodExecutor implements MethodExecutor {

	private final Invoker invoker;
	private final String methodName;
	private final boolean failOnUnresolvedExpression;

	public StandardMethodExecutor(String name, Invoker invoker, boolean failOnUnresolvedExpression) {
		this.failOnUnresolvedExpression = failOnUnresolvedExpression;
		this.invoker = invoker;
		this.methodName = name;
	}

	@Override
	@NonNull
	public TypedValue execute(
			@NonNull EvaluationContext context,
			@NonNull Object target,
			@NonNull Object... arguments
	) throws AccessException {
		try {
			return new TypedValue(invoker.invoke(arguments));
		} catch (InvocationTargetException | IllegalAccessException e) {
			if (failOnUnresolvedExpression) {
				throw new AccessException(String.format("Error calling method %s", methodName), e);
			} else {
				return new TypedValue(null);
			}
		}
	}

	@FunctionalInterface
	interface Invoker {
		Object invoke(Object... arguments) throws InvocationTargetException, IllegalAccessException;
	}
}
