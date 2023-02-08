package org.wickedsource.docxstamper.el;

import org.springframework.core.convert.TypeDescriptor;
import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.MethodExecutor;
import org.springframework.expression.MethodResolver;
import org.wickedsource.docxstamper.DocxStamperConfiguration;

import java.lang.reflect.Method;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;

public class StandardMethodResolver implements MethodResolver {
    private final DocxStamperConfiguration configuration;

    public StandardMethodResolver(DocxStamperConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public MethodExecutor resolve(EvaluationContext context, Object targetObject, String name, List<TypeDescriptor> argumentTypes) throws AccessException {
        Map.Entry<Method, Object> methodEntry = findCommentProcessorMethod(name, argumentTypes);

        if (methodEntry == null) {
            methodEntry = findExpressionContextMethod(name, argumentTypes);
        }

        if (methodEntry == null) return null;

        return new StandardMethodExecutor(configuration, methodEntry.getKey(), methodEntry.getValue());
    }

    private Map.Entry<Method, Object> findCommentProcessorMethod(String expectedName, List<TypeDescriptor> expectedArguments) {
        return findMethodInMap(configuration.getCommentProcessors(), expectedName, expectedArguments);
    }

    private Map.Entry<Method, Object> findExpressionContextMethod(String expectedName, List<TypeDescriptor> expectedArguments) {
        return findMethodInMap(configuration.getExpressionFunctions(), expectedName, expectedArguments);
    }

    private Map.Entry<Method, Object> findMethodInMap(Map<Class<?>, Object> methodMap, String expectedName, List<TypeDescriptor> expectedArguments) {
        for (Map.Entry<Class<?>, Object> entry : methodMap.entrySet()) {
            Class<?> iface = entry.getKey();

            for (Method actualMethod : iface.getDeclaredMethods()) {
                if (methodEquals(actualMethod, expectedName, expectedArguments)) {
                    return new AbstractMap.SimpleEntry<>(actualMethod, entry.getValue());
                }
            }
        }

        return null;
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
