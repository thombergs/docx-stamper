package org.wickedsource.docxstamper.processor.repeat;

import lombok.SneakyThrows;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.*;
import org.jvnet.jaxb2_commons.ppp.Child;
import org.wickedsource.docxstamper.api.DocxStamperException;
import org.wickedsource.docxstamper.api.commentprocessor.ICommentProcessor;
import org.wickedsource.docxstamper.processor.BaseCommentProcessor;
import org.wickedsource.docxstamper.replace.PlaceholderReplacer;
import org.wickedsource.docxstamper.util.CommentWrapper;
import org.wickedsource.docxstamper.util.DocumentUtil;
import org.wickedsource.docxstamper.util.ParagraphUtil;
import org.wickedsource.docxstamper.util.SectionUtil;
import pro.verron.docxstamper.OpcStamper;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toMap;
import static org.wickedsource.docxstamper.util.DocumentUtil.walkObjectsAndImportImages;

public class RepeatDocPartProcessor extends BaseCommentProcessor implements IRepeatDocPartProcessor {
	public static final ThreadFactory THREAD_FACTORY = Executors.defaultThreadFactory();
	private static final ObjectFactory objectFactory = Context.getWmlObjectFactory();
	private final OpcStamper<WordprocessingMLPackage> stamper;
	private final Map<CommentWrapper, List<Object>> contexts = new HashMap<>();
	private final Supplier<? extends List<?>> nullSupplier;

	private RepeatDocPartProcessor(
			PlaceholderReplacer placeholderReplacer,
			OpcStamper<WordprocessingMLPackage> stamper,
			Supplier<? extends List<?>> nullSupplier
	) {
		super(placeholderReplacer);
		this.stamper = stamper;
		this.nullSupplier = nullSupplier;
	}

	public static ICommentProcessor newInstance(PlaceholderReplacer pr, OpcStamper<WordprocessingMLPackage> stamper, String nullReplacementValue) {
		return new RepeatDocPartProcessor(pr, stamper, () -> singletonList(ParagraphUtil.create(nullReplacementValue)));
	}

	public static ICommentProcessor newInstance(PlaceholderReplacer pr, OpcStamper<WordprocessingMLPackage> stamper) {
		return new RepeatDocPartProcessor(pr, stamper, Collections::emptyList);
	}

	@Override
	public void repeatDocPart(List<Object> contexts) {
		if (contexts == null)
			contexts = Collections.emptyList();

		CommentWrapper currentCommentWrapper = getCurrentCommentWrapper();
		List<Object> repeatElements = currentCommentWrapper.getRepeatElements();

		if (!repeatElements.isEmpty()) {
			this.contexts.put(currentCommentWrapper, contexts);
		}
	}

	@SneakyThrows
	@Override
	public void commitChanges(WordprocessingMLPackage document) {
		for (Entry<CommentWrapper, List<Object>> entry : this.contexts.entrySet()) {
			CommentWrapper commentWrapper = entry.getKey();
			List<Object> expressionContexts = entry.getValue();
			ContentAccessor gcp = Objects.requireNonNull(commentWrapper.getParent());
			List<Object> repeatElements = commentWrapper.getRepeatElements();
			WordprocessingMLPackage subTemplate = commentWrapper.tryBuildingSubtemplate(document);
			SectPr previousSectionBreak = SectionUtil.getPreviousSectionBreakIfPresent(repeatElements.get(0), gcp);
			boolean oddNumberOfBreaks = SectionUtil.isOddNumberOfSectionBreaks(repeatElements);

			List<?> changes = expressionContexts == null
					? nullSupplier.get()
					: stampSubDocuments(document,
										expressionContexts,
										gcp,
										subTemplate,
										previousSectionBreak,
										oddNumberOfBreaks);

			List<Object> gcpContent = gcp.getContent();
			int index = gcpContent.indexOf(repeatElements.get(0));
			gcpContent.addAll(index, changes);
			gcpContent.removeAll(repeatElements);
		}
	}

	private List<Object> stampSubDocuments(WordprocessingMLPackage document, List<Object> expressionContexts, ContentAccessor gcp, WordprocessingMLPackage subTemplate, SectPr previousSectionBreak, boolean oddNumberOfBreaks) {
		Deque<WordprocessingMLPackage> subDocuments = stampSubDocuments(expressionContexts, subTemplate);
		Map<R, R> replacements = subDocuments
				.stream()
				.map(p -> walkObjectsAndImportImages(p, document)) // TODO: remove the side effect here
				.map(Map::entrySet)
				.flatMap(Set::stream)
				.collect(toMap(Entry::getKey, Entry::getValue));

		var changes = new ArrayList<>();
		for (WordprocessingMLPackage subDocument : subDocuments) {
			var os = documentAsInsertableElements(subDocument, oddNumberOfBreaks, previousSectionBreak);
			os.forEach(o -> recursivelyReplaceImages(o, replacements));
			os.forEach(c -> setParentIfPossible(c, gcp));
			changes.addAll(os);
		}
		return changes;
	}

	private Deque<WordprocessingMLPackage> stampSubDocuments(List<Object> subContexts, WordprocessingMLPackage subTemplate) {
		Deque<WordprocessingMLPackage> subDocuments = new ArrayDeque<>();
		for (Object subContext : subContexts) {
			WordprocessingMLPackage templateCopy = outputWord(os -> copy(subTemplate, os));
			WordprocessingMLPackage subDocument = outputWord(os -> stamp(subContext,
																		 templateCopy,
																		 os
			));
			subDocuments.add(subDocument);
		}
		return subDocuments;
	}

	private static List<Object> documentAsInsertableElements(WordprocessingMLPackage subDocument, boolean oddNumberOfBreaks, SectPr previousSectionBreak) {
		List<Object> inserts = new ArrayList<>(DocumentUtil.allElements(subDocument));
		// make sure we replicate the previous section break before each repeated doc part
		if (oddNumberOfBreaks && previousSectionBreak != null) {
			if (DocumentUtil.lastElement(subDocument) instanceof P p) {
				SectionUtil.applySectionBreakToParagraph(previousSectionBreak, p);
			} else {
				// when the last element to be repeated is not a paragraph, we need to add a new
				// one right after to carry the section break to have a valid xml
				P p = objectFactory.createP();
				SectionUtil.applySectionBreakToParagraph(previousSectionBreak, p);
				inserts.add(p);
			}
		}
		return inserts;
	}

	private static void recursivelyReplaceImages(Object o, Map<R, R> replacements) {
		Queue<Object> q = new ArrayDeque<>();
		q.add(o);
		while (!q.isEmpty()) {
			Object current = q.remove();
			if (replacements.containsKey(current)
					&& current instanceof Child child
					&& child.getParent() instanceof ContentAccessor parent) {
				List<Object> parentContent = parent.getContent();
				parentContent.add(parentContent.indexOf(current), replacements.get(current));
				parentContent.remove(current);
			} else if (current instanceof ContentAccessor ca) {
				q.addAll(ca.getContent());
			}
		}
	}

	private static void setParentIfPossible(Object object, ContentAccessor parent) {
		if (object instanceof Child child)
			child.setParent(parent);
	}

	private WordprocessingMLPackage outputWord(Consumer<OutputStream> outputter) {
		try (
				PipedOutputStream os = new PipedOutputStream();
				PipedInputStream is = new PipedInputStream(os)
		) {
			Thread thread = THREAD_FACTORY.newThread(() -> outputter.accept(os));
			thread.start();
			WordprocessingMLPackage wordprocessingMLPackage = WordprocessingMLPackage.load(is);
			thread.join();
			return wordprocessingMLPackage;

		} catch (Docx4JException | IOException | InterruptedException e) {
			throw new DocxStamperException(e);
		}
	}

	private void copy(WordprocessingMLPackage aPackage, OutputStream outputStream) {
		try {
			aPackage.save(outputStream);
		} catch (Docx4JException e) {
			throw new DocxStamperException(e);
		}
	}

	private void stamp(Object context, WordprocessingMLPackage template, OutputStream outputStream) {
		stamper.stamp(template, context, outputStream);
	}

	@Override
	public void reset() {
		contexts.clear();
	}
}
