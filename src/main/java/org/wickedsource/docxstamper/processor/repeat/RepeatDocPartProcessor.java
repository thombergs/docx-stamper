package org.wickedsource.docxstamper.processor.repeat;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.*;
import org.jvnet.jaxb2_commons.ppp.Child;
import org.wickedsource.docxstamper.api.typeresolver.TypeResolverRegistry;
import org.wickedsource.docxstamper.el.ExpressionResolver;
import org.wickedsource.docxstamper.processor.BaseCommentProcessor;
import org.wickedsource.docxstamper.processor.CommentProcessorRegistry;
import org.wickedsource.docxstamper.replace.PlaceholderReplacer;
import org.wickedsource.docxstamper.util.CommentUtil;
import org.wickedsource.docxstamper.util.CommentWrapper;
import org.wickedsource.docxstamper.util.walk.BaseDocumentWalker;
import org.wickedsource.docxstamper.util.walk.DocumentWalker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RepeatDocPartProcessor extends BaseCommentProcessor implements IRepeatDocPartProcessor {

    private Map<CommentWrapper, List<Object>> partsToRepeat = new HashMap<>();
    private PlaceholderReplacer<Object> placeholderReplacer;
    private CommentProcessorRegistry commentProcessorRegistry;

    public RepeatDocPartProcessor(TypeResolverRegistry typeResolverRegistry, ExpressionResolver expressionResolver) {
        this.placeholderReplacer = new PlaceholderReplacer<>(typeResolverRegistry);
        this.placeholderReplacer.setExpressionResolver(expressionResolver);

        this.commentProcessorRegistry = new CommentProcessorRegistry(this.placeholderReplacer);
        this.commentProcessorRegistry.setExpressionResolver(expressionResolver);
    }

    @Override
    public void repeatDocPart(List<Object> objects) {
        partsToRepeat.put(getCurrentCommentWrapper(), objects);
    }

    @Override
    public void commitChanges(WordprocessingMLPackage document) {
        for (CommentWrapper commentWrapper : partsToRepeat.keySet()) {
            List<Object> expressionContexts = partsToRepeat.get(commentWrapper);

            CommentRangeStart start = commentWrapper.getCommentRangeStart();

            ContentAccessor gcp = findGreatestCommonParent(commentWrapper.getCommentRangeEnd(), (ContentAccessor) start.getParent());
            List<Object> repeatElements = getRepeatElements(commentWrapper, gcp);
            int insertIndex = gcp.getContent().indexOf(repeatElements.stream().findFirst().orElse(null));

            CommentUtil.deleteComment(commentWrapper); // for deep copy without comment

            for (final Object expressionContext : expressionContexts) {
                for (final CommentWrapper comment : commentWrapper.getChildren()) {
                    System.out.println(comment);
                    // TODO : link the comment to the right repeatElement
                    // and then apply comment resolvers to it
                    // comment.getCommentRangeStart().parent.parent... -> until matching one of the elements in the list
                }

                for (final Object element : repeatElements) {
                    Object elClone = XmlUtils.unwrap(XmlUtils.deepCopy(element));
                    if (elClone instanceof P) {
                        placeholderReplacer.resolveExpressionsForParagraph((P) elClone, expressionContext, document);
                    } else if (elClone instanceof ContentAccessor) {
                        DocumentWalker walker = new BaseDocumentWalker((ContentAccessor) elClone) {
                            @Override
                            protected void onParagraph(P paragraph) {
                                placeholderReplacer.resolveExpressionsForParagraph(paragraph, expressionContext, document);
                            }
                        };
                        walker.walk();
                    }
                    gcp.getContent().add(insertIndex++, elClone);
                }
            }
            gcp.getContent().removeAll(repeatElements);
        }
    }

    @Override
    public void reset() {
        partsToRepeat = new HashMap<>();
    }

    private static List<Object> getRepeatElements(CommentWrapper commentWrapper, ContentAccessor greatestCommonParent) {
        List<Object> repeatElements = new ArrayList<>();
        boolean startFound = false;
        for (Object element : greatestCommonParent.getContent()) {
            if (!startFound
                    && depthElementSearch(commentWrapper.getCommentRangeStart(), element)) {
                startFound = true;
            }
            if (startFound) {
                repeatElements.add(element);

                if (depthElementSearch(commentWrapper.getCommentRangeEnd(), element)) {
                    break;
                }
            }
        }
        return repeatElements;
    }

    private static ContentAccessor findGreatestCommonParent(Object targetSearch, ContentAccessor searchFrom) {
        if (depthElementSearch(targetSearch, searchFrom)) {
            if (searchFrom instanceof Tr) { // if it's Tr - need add new line to table
                return (ContentAccessor) ((Tr) searchFrom).getParent();
            } else if (searchFrom instanceof Tc) { // if it's Tc - need add new cell to row
                return (ContentAccessor) ((Tc) searchFrom).getParent();
            }
            return searchFrom;
        }
        return findGreatestCommonParent(targetSearch, (ContentAccessor) ((Child) searchFrom).getParent());
    }

    private static boolean depthElementSearch(Object searchTarget, Object content) {
        content = XmlUtils.unwrap(content);
        if (searchTarget.equals(content)) {
            return true;
        } else if (content instanceof ContentAccessor) {
            for (Object object : ((ContentAccessor) content).getContent()) {
                Object unwrappedObject = XmlUtils.unwrap(object);
                if (searchTarget.equals(unwrappedObject)
                        || depthElementSearch(searchTarget, unwrappedObject)) {
                    return true;
                }
            }
        }
        return false;
    }
}
