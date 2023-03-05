package org.wickedsource.docxstamper.util;

import org.docx4j.utils.TraversalUtilVisitor;
import org.docx4j.wml.P;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class ParagraphCollector extends TraversalUtilVisitor<P> {
	private final List<P> paragraphs = new ArrayList<>();

	public Stream<P> paragraphs() {
		return paragraphs.stream();
	}

	@Override
	public void apply(P paragraph) {
		paragraphs.add(paragraph);
	}
}
