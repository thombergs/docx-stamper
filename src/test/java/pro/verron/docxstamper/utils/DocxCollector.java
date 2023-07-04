package pro.verron.docxstamper.utils;

import org.docx4j.TraversalUtil;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class DocxCollector<T> extends TraversalUtil.CallbackImpl {

	private final Set<T> elements = new LinkedHashSet<>();
	private final Class<T> type;

	public DocxCollector(Class<T> type) {
		super();
		this.type = type;
	}

	public List<Object> apply(Object o) {
		if (type.isInstance(o)) {
			elements.add(type.cast(o));
		}
		return List.of(elements);
	}

	public Stream<T> elements() {
		return elements.stream();
	}
}
