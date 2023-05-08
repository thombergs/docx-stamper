package org.wickedsource.docxstamper.el;

import org.springframework.expression.EvaluationContext;
import org.springframework.expression.MethodExecutor;
import org.springframework.expression.TypedValue;
import org.springframework.lang.NonNull;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Function;

public class StandardMethodExecutor implements MethodExecutor {

	private final Invoker invoker;
	private final Function<ReflectiveOperationException, TypedValue> onFail;

	public StandardMethodExecutor(Invoker invoker, Function<ReflectiveOperationException, TypedValue> onFail) {
		this.invoker = invoker;
		this.onFail = onFail;
	}

	@Override
	@NonNull
	public TypedValue execute(
			@NonNull EvaluationContext context,
			@NonNull Object target,
			@NonNull Object... arguments
	) {
		try {
			Object value = invoker.invoke(arguments);
			return new TypedValue(value);
		} catch (InvocationTargetException | IllegalAccessException e) {
			return onFail.apply(e);
		}
	}

	@FunctionalInterface
	interface Invoker {
		Object invoke(Object... arguments) throws InvocationTargetException, IllegalAccessException;
	}
}
