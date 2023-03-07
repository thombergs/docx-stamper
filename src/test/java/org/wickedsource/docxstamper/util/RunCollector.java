package org.wickedsource.docxstamper.util;

import org.docx4j.utils.TraversalUtilVisitor;
import org.docx4j.wml.R;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class RunCollector extends TraversalUtilVisitor<R> {
	private final List<R> paragraphs = new ArrayList<>();

	public Stream<R> runs() {
		return paragraphs.stream();
	}

	@Override
	public void apply(R paragraph) {
		paragraphs.add(paragraph);
	}
}
