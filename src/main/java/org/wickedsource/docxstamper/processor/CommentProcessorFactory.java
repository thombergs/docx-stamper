package org.wickedsource.docxstamper.processor;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.wickedsource.docxstamper.DocxStamper;
import org.wickedsource.docxstamper.DocxStamperConfiguration;
import org.wickedsource.docxstamper.OpcStamper;
import org.wickedsource.docxstamper.api.commentprocessor.ICommentProcessor;
import org.wickedsource.docxstamper.processor.displayif.DisplayIfProcessor;
import org.wickedsource.docxstamper.processor.repeat.ParagraphRepeatProcessor;
import org.wickedsource.docxstamper.processor.repeat.RepeatDocPartProcessor;
import org.wickedsource.docxstamper.processor.repeat.RepeatProcessor;
import org.wickedsource.docxstamper.processor.replaceExpression.ReplaceWithProcessor;
import org.wickedsource.docxstamper.processor.table.TableResolver;
import org.wickedsource.docxstamper.replace.PlaceholderReplacer;

public class CommentProcessorFactory {
	private final DocxStamperConfiguration configuration;

	public CommentProcessorFactory(DocxStamperConfiguration configuration) {
		this.configuration = configuration;
	}

	public ICommentProcessor repeatParagraph(PlaceholderReplacer pr) {
		return configuration
				.nullReplacementValue()
				.map(nullReplacementValue -> ParagraphRepeatProcessor.newInstance(pr, nullReplacementValue))
				.orElseGet(() -> ParagraphRepeatProcessor.newInstance(pr));
	}

	public ICommentProcessor repeatDocPart(PlaceholderReplacer pr) {
		return configuration
				.nullReplacementValue()
				.map(nullReplacementValue -> RepeatDocPartProcessor.newInstance(pr, getStamper(), nullReplacementValue))
				.orElseGet(() -> RepeatDocPartProcessor.newInstance(pr, getStamper()));
	}

	private OpcStamper<WordprocessingMLPackage> getStamper() {
		return (template, context, output) -> new DocxStamper<>(configuration).stamp(template, context, output);
	}

	public ICommentProcessor repeat(PlaceholderReplacer pr) {
		return configuration
				.nullReplacementValue()
				.map(nullReplacementValue -> RepeatProcessor.newInstanceWithNullReplacement(pr))
				.orElseGet(() -> RepeatProcessor.newInstance(pr));
	}

	public ICommentProcessor tableResolver(PlaceholderReplacer pr) {
		return configuration
				.nullReplacementValue()
				.map(nullReplacementValue -> TableResolver.newInstance(pr, nullReplacementValue))
				.orElseGet(() -> TableResolver.newInstance(pr));
	}

	public ICommentProcessor displayIf(PlaceholderReplacer pr) {
		return DisplayIfProcessor.newInstance(pr);
	}

	public ICommentProcessor replaceWith(PlaceholderReplacer pr) {
		return configuration
				.nullReplacementValue()
				.map(nullReplacementValue -> ReplaceWithProcessor.newInstance(pr, nullReplacementValue))
				.orElseGet(() -> ReplaceWithProcessor.newInstance(pr));
	}
}
