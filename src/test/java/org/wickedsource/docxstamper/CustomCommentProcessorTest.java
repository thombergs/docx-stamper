package org.wickedsource.docxstamper;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.junit.jupiter.api.Test;
import org.wickedsource.docxstamper.api.commentprocessor.ICommentProcessor;
import org.wickedsource.docxstamper.api.typeresolver.TypeResolverRegistry;
import org.wickedsource.docxstamper.processor.BaseCommentProcessor;
import org.wickedsource.docxstamper.util.CommentWrapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CustomCommentProcessorTest {

	@Test
	public void test() throws Docx4JException, IOException {
		var template = getClass().getResourceAsStream("CustomCommentProcessorTest.docx");
		var config = new DocxStamperConfiguration()
				.addCommentProcessor(ICustomCommentProcessor.class, CustomCommentProcessor.class);
		var stamper = new TestDocxStamper<>(config);
		stamper.stampAndLoad(template, new EmptyContext());

		assertEquals(2, CustomCommentProcessor.getVisitedParagraphs().size());
	}

	public interface ICustomCommentProcessor extends ICommentProcessor {
		void visitParagraph();
	}

	static class EmptyContext {
	}

	public static class CustomCommentProcessor extends BaseCommentProcessor implements ICustomCommentProcessor {

		private static final List<P> visitedParagraphs = new ArrayList<>();

		private P currentParagraph;

		public CustomCommentProcessor(DocxStamperConfiguration config, TypeResolverRegistry typeResolverRegistry) {
			super(config, typeResolverRegistry);
		}

		public static List<P> getVisitedParagraphs() {
			return visitedParagraphs;
		}

		@Override
		public void commitChanges(WordprocessingMLPackage document) {

		}

		@Override
		public void reset() {
		}

		@Override
		public void setCurrentRun(R run) {
		}

		@Override
		public void setParagraph(P paragraph) {
			currentParagraph = paragraph;
		}

		@Override
		public void setCurrentCommentWrapper(CommentWrapper commentWrapper) {
		}

		@Override
		public void setDocument(WordprocessingMLPackage document) {
		}

		@Override
		public void visitParagraph() {
			visitedParagraphs.add(currentParagraph);
		}
	}
}
