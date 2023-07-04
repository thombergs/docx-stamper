package pro.verron.docxstamper.utils;

import java.util.function.Supplier;

public interface ThrowingSupplier<T> extends Supplier<T> {
	default T get() {
		try {
			return throwingGet();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	T throwingGet() throws Exception;
}
