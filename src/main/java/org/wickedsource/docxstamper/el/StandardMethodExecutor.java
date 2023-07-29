package org.wickedsource.docxstamper.el;

import org.springframework.expression.EvaluationContext;
import org.springframework.expression.MethodExecutor;
import org.springframework.expression.TypedValue;
import org.springframework.lang.NonNull;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Function;

/**
 * This class is a wrapper around a method call which can be executed by the Spring Expression Language.
 * It is used by the {@link org.wickedsource.docxstamper.el.ExpressionResolver} to evaluate method calls.
 *
 * @author joseph
 * @version $Id: $Id
 */
public class StandardMethodExecutor implements MethodExecutor {

	private final Invoker invoker;
	private final Function<ReflectiveOperationException, TypedValue> onFail;

	/**
	 * <p>Constructor for StandardMethodExecutor.</p>
	 *
	 * @param invoker the invoker that is used to call the method in question.
	 * @param onFail  a function that is called if the invoker throws an exception. The function may return a default
	 *                value to be returned by the {@link #execute(EvaluationContext, Object, Object...)} method.
	 */
	public StandardMethodExecutor(Invoker invoker, Function<ReflectiveOperationException, TypedValue> onFail) {
		this.invoker = invoker;
		this.onFail = onFail;
	}

	/** {@inheritDoc} */
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

	/**
	 * The invoker is used to call the method in question.
	 */
	@FunctionalInterface
	public interface Invoker {
		/**
		 * Should call the expected method with the given arguments
		 * @param arguments list of arguments to send to the method
		 * @return the result of the method call
		 * @throws InvocationTargetException if the method does not accept the expected parameters
		 * @throws IllegalAccessException if the method is not accessible from calling code
		 */
		Object invoke(Object... arguments) throws InvocationTargetException, IllegalAccessException;
	}
}
