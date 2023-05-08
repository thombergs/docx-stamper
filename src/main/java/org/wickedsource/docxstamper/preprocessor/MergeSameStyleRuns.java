package org.wickedsource.docxstamper.preprocessor;

import org.docx4j.TraversalUtil;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.utils.TraversalUtilVisitor;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.R;
import org.docx4j.wml.RPr;
import org.wickedsource.docxstamper.api.preprocessor.PreProcessor;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

public class MergeSameStyleRuns implements PreProcessor {
	private final List<List<R>> similarStyleConcurrentRuns = new ArrayList<>();
	private final TraversalUtilVisitor<R> visitor = new TraversalUtilVisitor<>() {
		@Override
		public void apply(R element, Object parent, List<Object> siblings) {
			RPr rPr = element.getRPr();
			int currentIndex = siblings.indexOf(element);
			List<R> similarStyleConcurrentRun = siblings
					.stream()
					.skip(currentIndex)
					.takeWhile(o -> o instanceof R run && Objects.equals(run.getRPr(), rPr))
					.map(R.class::cast)
					.toList();

			if (similarStyleConcurrentRun.size() > 1)
				similarStyleConcurrentRuns.add(similarStyleConcurrentRun);
		}
	};

	@Override
	public void process(WordprocessingMLPackage document) {
		var mainDocumentPart = document.getMainDocumentPart();
		TraversalUtil.visit(mainDocumentPart, visitor);
		for (List<R> similarStyleConcurrentRun : similarStyleConcurrentRuns) {
			R firstRun = similarStyleConcurrentRun.get(0);
			var firstRunContent = new LinkedHashSet<>(firstRun.getContent());
			var firstRunParentContent = ((ContentAccessor) firstRun.getParent()).getContent();
			for (R r : similarStyleConcurrentRun.subList(1, similarStyleConcurrentRun.size())) {
				firstRunParentContent.remove(r);
				firstRunContent.addAll(r.getContent());
			}
			firstRun.getContent().clear();
			firstRun.getContent().addAll(firstRunContent);
		}
	}
}
