package org.wickedsource.docxstamper.el;

import org.springframework.core.convert.TypeDescriptor;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.MethodExecutor;
import org.springframework.expression.MethodResolver;
import org.springframework.lang.NonNull;
import org.wickedsource.docxstamper.DocxStamperConfiguration;

import java.lang.reflect.Method;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class StandardMethodResolver implements MethodResolver {
	private final DocxStamperConfiguration configuration;

	public StandardMethodResolver(DocxStamperConfiguration configuration) {
		this.configuration = configuration;
	}

	@Override
	public MethodExecutor resolve(
			@NonNull EvaluationContext context,
			@NonNull Object targetObject,
			@NonNull String name,
			@NonNull List<TypeDescriptor> argumentTypes
	) {
		return findCommentProcessorMethod(name, argumentTypes)
				.or(() -> findExpressionContextMethod(name, argumentTypes))
				.map(methodEntry -> new StandardMethodExecutor(
						methodEntry.getKey().getName(),
						(args) -> methodEntry.getKey().invoke(methodEntry.getValue(), args),
						configuration.isFailOnUnresolvedExpression()))
				.orElse(null);

	}

	private Optional<Map.Entry<Method, Object>> findCommentProcessorMethod(String expectedName, List<TypeDescriptor> expectedArguments) {
		return findMethodInMap(configuration.getCommentProcessors(), expectedName, expectedArguments);
	}

	private Optional<Map.Entry<Method, Object>> findExpressionContextMethod(String expectedName, List<TypeDescriptor> expectedArguments) {
		return findMethodInMap(configuration.getExpressionFunctions(), expectedName, expectedArguments);
	}

	private Optional<Map.Entry<Method, Object>> findMethodInMap(Map<Class<?>, Object> methodMap, String expectedName, List<TypeDescriptor> expectedArguments) {
		for (Map.Entry<Class<?>, Object> entry : methodMap.entrySet()) {
			Class<?> iface = entry.getKey();
			for (Method actualMethod : iface.getDeclaredMethods()) {
				if (methodEquals(actualMethod, expectedName, expectedArguments)) {
					return Optional.of(new AbstractMap.SimpleEntry<>(actualMethod, entry.getValue()));
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
