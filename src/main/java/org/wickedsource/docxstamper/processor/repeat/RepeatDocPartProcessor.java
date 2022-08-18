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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RepeatDocPartProcessor extends BaseCommentProcessor implements IRepeatDocPartProcessor {

    private Map<CommentWrapper, List<Object>> subContexts = new HashMap<>();
    private Map<CommentWrapper, WordprocessingMLPackage> subTemplates = new HashMap();

    private PlaceholderReplacer<Object> placeholderReplacer;
    private CommentProcessorRegistry commentProcessorRegistry;

    public RepeatDocPartProcessor(TypeResolverRegistry typeResolverRegistry, ExpressionResolver expressionResolver) {
        this.placeholderReplacer = new PlaceholderReplacer<>(typeResolverRegistry);
        this.placeholderReplacer.setExpressionResolver(expressionResolver);

        this.commentProcessorRegistry = new CommentProcessorRegistry(this.placeholderReplacer);
        this.commentProcessorRegistry.setExpressionResolver(expressionResolver);
    }

    @Override
    public void repeatDocPart(List<Object> contexts) {
        CommentWrapper currentCommentWrapper = getCurrentCommentWrapper();
        subContexts.put(currentCommentWrapper, contexts);
        subTemplates.put(currentCommentWrapper, extractSubTemplate(currentCommentWrapper));
    }

    @Override
    public void commitChanges(WordprocessingMLPackage document) {
        for (CommentWrapper commentWrapper : subContexts.keySet()) {
            List<Object> expressionContexts = subContexts.get(commentWrapper);
            WordprocessingMLPackage subTemplate = subTemplates.get(commentWrapper);

            CommentRangeStart start = commentWrapper.getCommentRangeStart();

            // TODO : generate sub doc and insert content back in the document
        }
    }

    @Override
    public void reset() {
        subContexts = new HashMap<>();
        subTemplates = new HashMap();
    }

    private static WordprocessingMLPackage extractSubTemplate(CommentWrapper commentWrapper) {
        CommentRangeStart start = commentWrapper.getCommentRangeStart();

        ContentAccessor gcp = findGreatestCommonParent(commentWrapper.getCommentRangeEnd(), (ContentAccessor) start.getParent());
        List<Object> repeatElements = getRepeatElements(commentWrapper, gcp);
        CommentUtil.deleteComment(commentWrapper); // for deep copy without comment

        WordprocessingMLPackage document = null;

        try {
            document = WordprocessingMLPackage.createPackage();
            document.getMainDocumentPart().getContent().addAll(repeatElements);
            List<Comments.Comment> comments = new ArrayList<>();
            for (CommentWrapper wrapper : commentWrapper.getChildren()) {
                Comments.Comment comment = wrapper.getComment();
                comments.add(comment);
            }
            document.save(new File("temp.docx"));
        } catch (Exception e) {
            System.out.println(e);
        }

        // TODO insert child comments in the template

        return document;
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
