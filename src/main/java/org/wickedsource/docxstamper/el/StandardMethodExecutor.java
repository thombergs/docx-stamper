package org.wickedsource.docxstamper.el;

import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.MethodExecutor;
import org.springframework.expression.TypedValue;
import org.wickedsource.docxstamper.DocxStamperConfiguration;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class StandardMethodExecutor implements MethodExecutor {
    private final DocxStamperConfiguration configuration;
    private final Method method;
    private final Object implementation;

    public StandardMethodExecutor(DocxStamperConfiguration configuration, Method method, Object implementation) {
        this.configuration = configuration;
        this.method = method;
        this.implementation = implementation;
    }

    @Override
    public TypedValue execute(EvaluationContext context, Object target, Object... arguments) throws AccessException {
        try {
            return new TypedValue(method.invoke(implementation, arguments));
        } catch (InvocationTargetException | IllegalAccessException e) {
            if (configuration.isFailOnUnresolvedExpression()) {
                throw new AccessException(String.format("Error calling method %s", method.getName()), e);
            } else {
                return new TypedValue(null);
            }
        }
    }
}
