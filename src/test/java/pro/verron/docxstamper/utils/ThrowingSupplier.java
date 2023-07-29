package pro.verron.docxstamper.utils;

import java.util.function.Supplier;

/**
 * <p>ThrowingSupplier interface.</p>
 *
 * @author joseph
 * @version $Id: $Id
 * @since 1.6.5
 */
public interface ThrowingSupplier<T> extends Supplier<T> {
	/**
	 * <p>get.</p>
	 *
	 * @return a T object
	 */
	default T get() {
		try {
			return throwingGet();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * <p>throwingGet.</p>
	 *
	 * @return a T object
	 * @throws java.lang.Exception if any.
	 */
	T throwingGet() throws Exception;
}
