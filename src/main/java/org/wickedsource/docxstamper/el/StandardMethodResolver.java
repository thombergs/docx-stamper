package org.wickedsource.docxstamper.el;

import org.springframework.core.convert.TypeDescriptor;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.MethodExecutor;
import org.springframework.expression.MethodResolver;
import org.springframework.expression.TypedValue;
import org.springframework.lang.NonNull;
import org.wickedsource.docxstamper.api.DocxStamperException;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class StandardMethodResolver implements MethodResolver {
	private final boolean failOnUnresolvedExpression;
	private final Map<Class<?>, Object> commentProcessors;
	private final Map<Class<?>, Object> expressionFunctions;

	public StandardMethodResolver(
			boolean failOnUnresolvedExpression1,
			Map<Class<?>, Object> commentProcessors1,
			Map<Class<?>, Object> expressionFunctions1
	) {
		failOnUnresolvedExpression = failOnUnresolvedExpression1;
		commentProcessors = commentProcessors1;
		expressionFunctions = expressionFunctions1;
	}

	private static TypedValue throwException(String name, ReflectiveOperationException exception) {
		String message = String.format("Error calling method %s", name);
		throw new DocxStamperException(message, exception);
	}

	@Override
	public MethodExecutor resolve(
			@NonNull EvaluationContext context,
			@NonNull Object targetObject,
			@NonNull String name,
			@NonNull List<TypeDescriptor> argumentTypes
	) {
		Function<ReflectiveOperationException, TypedValue> onFail = failOnUnresolvedExpression
				? exception -> throwException(name, exception)
				: exception -> new TypedValue(null);

		return findCommentProcessorMethod(name, argumentTypes)
				.or(() -> findExpressionContextMethod(name, argumentTypes))
				.map(invoker -> new StandardMethodExecutor(invoker, onFail))
				.orElse(null);
	}

	private Optional<StandardMethodExecutor.Invoker> findCommentProcessorMethod(String expectedName, List<TypeDescriptor> expectedArguments) {
		return findMethodInMap(commentProcessors, expectedName, expectedArguments);
	}

	private Optional<StandardMethodExecutor.Invoker> findExpressionContextMethod(String expectedName, List<TypeDescriptor> expectedArguments) {
		return findMethodInMap(expressionFunctions, expectedName, expectedArguments);
	}

	private Optional<StandardMethodExecutor.Invoker> findMethodInMap(Map<Class<?>, Object> methodMap, String expectedName, List<TypeDescriptor> expectedArguments) {
		for (Map.Entry<Class<?>, Object> entry : methodMap.entrySet()) {
			Class<?> iface = entry.getKey();
			for (Method actualMethod : iface.getDeclaredMethods()) {
				if (methodEquals(actualMethod, expectedName, expectedArguments)) {
					return Optional.of((args) -> actualMethod.invoke(entry.getValue(), args));
				}
			}
		}
		return Optional.empty();
	}

	private boolean methodEquals(Method actualMethod, String expectedName, List<TypeDescriptor> expectedArguments) {
		if (!actualMethod.getName().equals(expectedName)) return false;
		if (actualMethod.getParameterTypes().length != expectedArguments.size()) return false;

		for (int i = 0; i < expectedArguments.size(); i++) {
			Class<?> expectedType = expectedArguments.get(i) != null ? expectedArguments.get(i).getType() : null;
			Class<?> actualType = actualMethod.getParameterTypes()[i];
			// null is allowed in place of any type of argument
			if (expectedType != null && !actualType.isAssignableFrom(expectedType)) {
				return false;
			}
		}
		return true;
	}
}
