package org.wickedsource.docxstamper.processor.repeat;

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.CommentsPart;
import org.docx4j.wml.*;
import org.jvnet.jaxb2_commons.ppp.Child;
import org.wickedsource.docxstamper.DocxStamper;
import org.wickedsource.docxstamper.DocxStamperConfiguration;
import org.wickedsource.docxstamper.api.typeresolver.TypeResolverRegistry;
import org.wickedsource.docxstamper.el.ExpressionResolver;
import org.wickedsource.docxstamper.processor.BaseCommentProcessor;
import org.wickedsource.docxstamper.processor.CommentProcessorRegistry;
import org.wickedsource.docxstamper.replace.PlaceholderReplacer;
import org.wickedsource.docxstamper.util.CommentUtil;
import org.wickedsource.docxstamper.util.CommentWrapper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RepeatDocPartProcessor extends BaseCommentProcessor implements IRepeatDocPartProcessor {

    private final DocxStamperConfiguration config;

    private Map<CommentWrapper, List<Object>> subContexts = new HashMap<>();
    private Map<CommentWrapper, WordprocessingMLPackage> subTemplates = new HashMap();
    private Map<CommentWrapper, Integer> insertIndex = new HashMap<>();

    private final PlaceholderReplacer<Object> placeholderReplacer;
    private final CommentProcessorRegistry commentProcessorRegistry;

    private final ObjectFactory objectFactory;

    int count = 0;

    public RepeatDocPartProcessor(TypeResolverRegistry typeResolverRegistry, ExpressionResolver expressionResolver, DocxStamperConfiguration config) {
        this.config = config;

        this.objectFactory = Context.getWmlObjectFactory();

        this.placeholderReplacer = new PlaceholderReplacer<>(typeResolverRegistry);
        this.placeholderReplacer.setExpressionResolver(expressionResolver);

        this.commentProcessorRegistry = new CommentProcessorRegistry(this.placeholderReplacer);
        this.commentProcessorRegistry.setExpressionResolver(expressionResolver);
    }

    @Override
    public void repeatDocPart(List<Object> contexts) {
        CommentWrapper currentCommentWrapper = getCurrentCommentWrapper();
        ContentAccessor gcp = findGreatestCommonParent(currentCommentWrapper.getCommentRangeEnd(), (ContentAccessor) currentCommentWrapper.getCommentRangeStart().getParent());
        List<Object> repeatElements = getRepeatElements(currentCommentWrapper, gcp);

        subContexts.put(currentCommentWrapper, contexts);
        insertIndex.put(currentCommentWrapper, gcp.getContent().indexOf(repeatElements.stream().findFirst().orElse(null)));
        subTemplates.put(currentCommentWrapper, extractSubTemplate(currentCommentWrapper, repeatElements));
    }

    private WordprocessingMLPackage copyTemplate(WordprocessingMLPackage doc) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            doc.save(baos);
            return WordprocessingMLPackage.load(new ByteArrayInputStream(baos.toByteArray()));
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }

    @Override
    public void commitChanges(WordprocessingMLPackage document) {
        for (CommentWrapper commentWrapper : subContexts.keySet()) {
            List<Object> expressionContexts = subContexts.get(commentWrapper);

            List<Object> changes = new ArrayList<>();

            expressionContexts.forEach(subContext -> {
                try {
                    WordprocessingMLPackage subTemplate = copyTemplate(subTemplates.get(commentWrapper));
                    DocxStamper<Object> stamper = new DocxStamper<>(config);
                    ByteArrayOutputStream output = new ByteArrayOutputStream();
                    stamper.stamp(subTemplate, subContext, output);
                    WordprocessingMLPackage subDocument = WordprocessingMLPackage.load(new ByteArrayInputStream(output.toByteArray()));
                    changes.addAll(subDocument.getMainDocumentPart().getContent());
                    subDocument.save(new File("subdoc-" + count + ".docx"));
                } catch (Exception e) {
                    System.out.println(e);
                }
                count++;
            });

            // TODO debug this part
            if (!changes.isEmpty()) {
                ContentAccessor gcp = findInsertableParent((ContentAccessor) commentWrapper.getCommentRangeStart().getParent());
                gcp.getContent().addAll(changes);
            }
        }
    }

    @Override
    public void reset() {
        subContexts = new HashMap<>();
        subTemplates = new HashMap<>();
        insertIndex = new HashMap<>();
    }

    private WordprocessingMLPackage extractSubTemplate(CommentWrapper commentWrapper, List<Object> repeatElements) {
        CommentUtil.deleteComment(commentWrapper); // for deep copy without comment

        WordprocessingMLPackage document = null;

        try {
            document = WordprocessingMLPackage.createPackage();
            CommentsPart commentsPart = new CommentsPart();
            document.getMainDocumentPart().addTargetPart(commentsPart);

            document.getMainDocumentPart().getContent().addAll(repeatElements);

            Comments comments = objectFactory.createComments();
            commentWrapper.getChildren().forEach(comment -> comments.getComment().add(comment.getComment()));
            commentsPart.setContents(comments);

            document.save(new File("temp.docx"));
        } catch (Exception e) {
            System.out.println(e);
        }

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
                if (depthElementSearch(commentWrapper.getCommentRangeEnd(), element)) {
                    break;
                }
                repeatElements.add(element);
            }
        }
        return repeatElements;
    }

    private static ContentAccessor findInsertableParent(ContentAccessor searchFrom) {
        if (searchFrom instanceof Tc) { // if it's Tc - need add new cell to row
            return searchFrom;
        }
        return findInsertableParent((ContentAccessor) ((Child) searchFrom).getParent());
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
