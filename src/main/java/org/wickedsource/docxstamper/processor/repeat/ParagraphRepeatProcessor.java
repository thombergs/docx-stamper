package org.wickedsource.docxstamper.processor.repeat;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.CommentRangeEnd;
import org.docx4j.wml.CommentRangeStart;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.P;
import org.wickedsource.docxstamper.DocxStamperConfiguration;
import org.wickedsource.docxstamper.api.coordinates.ParagraphCoordinates;
import org.wickedsource.docxstamper.api.typeresolver.TypeResolverRegistry;
import org.wickedsource.docxstamper.processor.BaseCommentProcessor;
import org.wickedsource.docxstamper.replace.PlaceholderReplacer;
import org.wickedsource.docxstamper.util.CommentUtil;
import org.wickedsource.docxstamper.util.ParagraphUtil;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParagraphRepeatProcessor extends BaseCommentProcessor implements IParagraphRepeatProcessor {

    private static class ParagraphsToRepeat {
        List<Object> data;
        List<P> paragraphs;
    }

    private Map<ParagraphCoordinates, ParagraphsToRepeat> pToRepeat = new HashMap<>();

    private final PlaceholderReplacer placeholderReplacer;
    private final DocxStamperConfiguration config;

    public ParagraphRepeatProcessor(TypeResolverRegistry typeResolverRegistry, DocxStamperConfiguration config) {
        this.placeholderReplacer = new PlaceholderReplacer(typeResolverRegistry, config);
        this.config = config;
    }

    @Override
    public void repeatParagraph(List<Object> objects) {
        ParagraphCoordinates paragraphCoordinates = getCurrentParagraphCoordinates();

        P paragraph = paragraphCoordinates.getParagraph();
        List<P> paragraphs = getParagraphsInsideComment(paragraph);

        ParagraphsToRepeat toRepeat = new ParagraphsToRepeat();
        toRepeat.data = objects;
        toRepeat.paragraphs = paragraphs;

        pToRepeat.put(paragraphCoordinates, toRepeat);
        CommentUtil.deleteComment(getCurrentCommentWrapper());
    }

    @Override
    public void commitChanges(WordprocessingMLPackage document) {
        for (ParagraphCoordinates rCoords : pToRepeat.keySet()) {
            ParagraphsToRepeat paragraphsToRepeat = pToRepeat.get(rCoords);
            List<Object> expressionContexts = paragraphsToRepeat.data;

            List<P> paragraphsToAdd = new ArrayList<>();

            if (expressionContexts != null) {
                for (final Object expressionContext : expressionContexts) {
                    for (P paragraphToClone : paragraphsToRepeat.paragraphs) {
                        P pClone = XmlUtils.deepCopy(paragraphToClone);
                        placeholderReplacer.resolveExpressionsForParagraph(pClone, expressionContext, document);

                        paragraphsToAdd.add(pClone);
                    }
                }
            } else if (config.isReplaceNullValues() && config.getNullValuesDefault() != null) {
                paragraphsToAdd.add(ParagraphUtil.create(config.getNullValuesDefault()));
            }

            Object parent = rCoords.getParagraph().getParent();
            if (parent instanceof ContentAccessor) {
                ContentAccessor contentAccessor = (ContentAccessor) parent;
                int index = contentAccessor.getContent().indexOf(rCoords.getParagraph());
                if (index >= 0) {
                    contentAccessor.getContent().addAll(index, paragraphsToAdd);
                }

                contentAccessor.getContent().removeAll(paragraphsToRepeat.paragraphs);
            }
        }
    }

    @Override
    public void reset() {
        pToRepeat = new HashMap<>();
    }

    public static List<P> getParagraphsInsideComment(P paragraph) {
        BigInteger commentId = null;
        boolean foundEnd = false;

        List<P> paragraphs = new ArrayList<>();
        paragraphs.add(paragraph);

        for (Object object : paragraph.getContent()) {
            if (object instanceof CommentRangeStart) {
                commentId = ((CommentRangeStart) object).getId();
            }
            if (object instanceof CommentRangeEnd && commentId != null && commentId.equals(((CommentRangeEnd) object).getId())) {
                foundEnd = true;
            }
        }
        if (!foundEnd && commentId != null) {
            Object parent = paragraph.getParent();
            if (parent instanceof ContentAccessor) {
                ContentAccessor contentAccessor = (ContentAccessor) parent;
                int index = contentAccessor.getContent().indexOf(paragraph);
                for (int i = index + 1; i < contentAccessor.getContent().size() && !foundEnd; i++) {
                    Object next = contentAccessor.getContent().get(i);

                    if (next instanceof CommentRangeEnd && ((CommentRangeEnd) next).getId().equals(commentId)) {
                        foundEnd = true;
                    } else {
                        if (next instanceof P) {
                            paragraphs.add((P) next);
                        }
                        if (next instanceof ContentAccessor) {
                            ContentAccessor childContent = (ContentAccessor) next;
                            for (Object child : childContent.getContent()) {
                                if (child instanceof CommentRangeEnd && ((CommentRangeEnd) child).getId().equals(commentId)) {
                                    foundEnd = true;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        return paragraphs;
    }
}
