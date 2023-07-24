package pro.verron.docxstamper.utils;

import org.docx4j.utils.TraversalUtilVisitor;
import org.docx4j.wml.R;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Stream;

public class RunCollector extends TraversalUtilVisitor<R> {
	private final Set<R> paragraphs = new LinkedHashSet<>();

	public Stream<R> runs() {
		return paragraphs.stream();
	}

	@Override
	public void apply(R paragraph) {
		paragraphs.add(paragraph);
	}
}
