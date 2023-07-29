package org.wickedsource.docxstamper.processor.repeat;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.*;
import org.wickedsource.docxstamper.api.DocxStamperException;
import org.wickedsource.docxstamper.api.commentprocessor.ICommentProcessor;
import org.wickedsource.docxstamper.processor.BaseCommentProcessor;
import org.wickedsource.docxstamper.replace.PlaceholderReplacer;
import org.wickedsource.docxstamper.util.CommentUtil;
import org.wickedsource.docxstamper.util.CommentWrapper;
import org.wickedsource.docxstamper.util.ParagraphUtil;
import org.wickedsource.docxstamper.util.SectionUtil;

import java.math.BigInteger;
import java.util.*;
import java.util.function.Supplier;

import static java.util.Collections.singletonList;

/**
 * This class is used to repeat paragraphs and tables.
 * <p>
 * It is used internally by the DocxStamper and should not be instantiated by
 * clients.
 *
 * @author joseph
 * @version $Id: $Id
 */
public class ParagraphRepeatProcessor extends BaseCommentProcessor implements IParagraphRepeatProcessor {
	private final Supplier<? extends List<? extends P>> nullSupplier;
	private Map<P, Paragraphs> pToRepeat = new HashMap<>();

	/**
	 * @param placeholderReplacer replaces placeholders with values
	 * @param nullSupplier        supplies a list of paragraphs if the list of objects to repeat is null
	 */
	private ParagraphRepeatProcessor(
			PlaceholderReplacer placeholderReplacer,
			Supplier<? extends List<? extends P>> nullSupplier
	) {
		super(placeholderReplacer);
		this.nullSupplier = nullSupplier;
	}

	/**
	 * <p>newInstance.</p>
	 *
	 * @param pr              replaces placeholders with values
	 * @param nullReplacement replaces null values
	 * @return a new instance of ParagraphRepeatProcessor
	 */
	public static ICommentProcessor newInstance(PlaceholderReplacer pr, String nullReplacement) {
		return new ParagraphRepeatProcessor(pr, () -> singletonList(ParagraphUtil.create(nullReplacement)));
	}

	/**
	 * <p>newInstance.</p>
	 *
	 * @param placeholderReplacer replaces placeholders with values
	 * @return a new instance of ParagraphRepeatProcessor
	 */
	public static ICommentProcessor newInstance(PlaceholderReplacer placeholderReplacer) {
		return new ParagraphRepeatProcessor(placeholderReplacer, Collections::emptyList);
	}

	/**
	 * Returns all paragraphs inside the comment of the given paragraph.
	 * <p>
	 * If the paragraph is not inside a comment, the given paragraph is returned.
	 *
	 * @param paragraph the paragraph to analyze
	 * @return all paragraphs inside the comment of the given paragraph
	 */
	public static Deque<P> getParagraphsInsideComment(P paragraph) {
		BigInteger commentId = null;
		boolean foundEnd = false;

		Deque<P> paragraphs = new ArrayDeque<>();
		paragraphs.add(paragraph);

		for (Object object : paragraph.getContent()) {
			if (object instanceof CommentRangeStart crs) commentId = crs.getId();
			if (object instanceof CommentRangeEnd cre && Objects.equals(commentId, cre.getId())) foundEnd = true;
		}
		if (foundEnd || commentId == null) return paragraphs;

		Object parent = paragraph.getParent();
		if (parent instanceof ContentAccessor contentAccessor) {
			int index = contentAccessor.getContent().indexOf(paragraph);
			for (int i = index + 1; i < contentAccessor.getContent().size() && !foundEnd; i++) {
				Object next = contentAccessor.getContent().get(i);

				if (next instanceof CommentRangeEnd cre && cre.getId().equals(commentId)) {
					foundEnd = true;
				} else {
					if (next instanceof P p) {
						paragraphs.add(p);
					}
					if (next instanceof ContentAccessor childContent) {
						for (Object child : childContent.getContent()) {
							if (child instanceof CommentRangeEnd cre && cre.getId().equals(commentId)) {
								foundEnd = true;
								break;
							}
						}
					}
				}
			}
		}
		return paragraphs;
	}

	private static void restoreFirstSectionBreakIfNeeded(Paragraphs paragraphs, Deque<P> paragraphsToAdd) {
		if (paragraphs.firstParagraphSectionBreak != null) {
			P breakP = paragraphsToAdd.getLast();
			SectionUtil.applySectionBreakToParagraph(paragraphs.firstParagraphSectionBreak, breakP);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void repeatParagraph(List<Object> objects) {
		P paragraph = getParagraph();

		Deque<P> paragraphs = getParagraphsInsideComment(paragraph);

		Paragraphs toRepeat = new Paragraphs();
		toRepeat.commentWrapper = getCurrentCommentWrapper();
		toRepeat.data = new ArrayDeque<>(objects);
		toRepeat.paragraphs = paragraphs;
		toRepeat.sectionBreakBefore = SectionUtil.getPreviousSectionBreakIfPresent(paragraph,
				(ContentAccessor) paragraph.getParent());
		toRepeat.firstParagraphSectionBreak = SectionUtil.getParagraphSectionBreak(paragraph);
		toRepeat.hasOddSectionBreaks = SectionUtil.isOddNumberOfSectionBreaks(new ArrayList<>(toRepeat.paragraphs));

		if (paragraph.getPPr() != null && paragraph.getPPr().getSectPr() != null) {
			// we need to clear the first paragraph's section break to be able to control how to repeat it
			paragraph.getPPr().setSectPr(null);
		}

		pToRepeat.put(paragraph, toRepeat);
	}

	private Deque<P> generateParagraphsToAdd(WordprocessingMLPackage document, Paragraphs paragraphs, Deque<Object> expressionContexts) {
		Deque<P> paragraphsToAdd = new ArrayDeque<>();

		Object lastExpressionContext = expressionContexts.peekLast();
		P lastParagraph = paragraphs.paragraphs.peekLast();

		for (Object expressionContext : expressionContexts) {
			for (P paragraphToClone : paragraphs.paragraphs) {
				P pClone = XmlUtils.deepCopy(paragraphToClone);

				if (paragraphs.sectionBreakBefore != null
						&& paragraphs.hasOddSectionBreaks
						&& expressionContext != lastExpressionContext
						&& paragraphToClone == lastParagraph
				) {
					SectionUtil.applySectionBreakToParagraph(paragraphs.sectionBreakBefore, pClone);
				}

				CommentUtil.deleteCommentFromElement(pClone.getContent(), paragraphs.commentWrapper.getComment().getId());
				placeholderReplacer.resolveExpressionsForParagraph(pClone, expressionContext, document);
				paragraphsToAdd.add(pClone);
			}
		}
		return paragraphsToAdd;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void commitChanges(WordprocessingMLPackage document) {
		for (Map.Entry<P, Paragraphs> entry : pToRepeat.entrySet()) {
			P currentP = entry.getKey();
			ContentAccessor parent = (ContentAccessor) currentP.getParent();
			List<Object> parentContent = parent.getContent();
			int index = parentContent.indexOf(currentP);
			if (index < 0) throw new DocxStamperException("Impossible");

			Paragraphs paragraphsToRepeat = entry.getValue();
			Deque<Object> expressionContexts = Objects.requireNonNull(paragraphsToRepeat).data;
			Deque<P> collection = expressionContexts == null
					? new ArrayDeque<>(nullSupplier.get())
					: generateParagraphsToAdd(document, paragraphsToRepeat, expressionContexts);
			restoreFirstSectionBreakIfNeeded(paragraphsToRepeat, collection);
			parentContent.addAll(index, collection);
			parentContent.removeAll(paragraphsToRepeat.paragraphs);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void reset() {
		pToRepeat = new HashMap<>();
	}

	private static class Paragraphs {
		CommentWrapper commentWrapper;
		Deque<Object> data;
		Deque<P> paragraphs;
		// hasOddSectionBreaks is true if the paragraphs to repeat contain an odd number of section breaks
		// changing the layout, false otherwise
		boolean hasOddSectionBreaks;
		// section break right before the first paragraph to repeat if present, or null
		SectPr sectionBreakBefore;
		// section break on the first paragraph to repeat if present, or null
		SectPr firstParagraphSectionBreak;
	}
}
