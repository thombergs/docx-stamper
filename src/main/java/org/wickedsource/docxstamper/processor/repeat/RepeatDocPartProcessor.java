package org.wickedsource.docxstamper.processor.repeat;

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.CommentsPart;
import org.docx4j.wml.*;
import org.jvnet.jaxb2_commons.ppp.Child;
import org.wickedsource.docxstamper.DocxStamper;
import org.wickedsource.docxstamper.DocxStamperConfiguration;
import org.wickedsource.docxstamper.processor.BaseCommentProcessor;
import org.wickedsource.docxstamper.util.CommentUtil;
import org.wickedsource.docxstamper.util.CommentWrapper;
import org.wickedsource.docxstamper.util.DocumentUtil;
import org.wickedsource.docxstamper.util.ParagraphUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.*;

public class RepeatDocPartProcessor extends BaseCommentProcessor implements IRepeatDocPartProcessor {

    private final DocxStamperConfiguration config;

    private Map<CommentWrapper, List<Object>> subContexts = new HashMap<>();
    private Map<CommentWrapper, List<Object>> repeatingElementsMap = new HashMap<>();
    private Map<CommentWrapper, WordprocessingMLPackage> subTemplates = new HashMap<>();
    private Map<CommentWrapper, ContentAccessor> gcpMap = new HashMap<>();
    private final ObjectFactory objectFactory;

    public RepeatDocPartProcessor(DocxStamperConfiguration config) {
        this.config = config;
        this.objectFactory = Context.getWmlObjectFactory();
    }

    @Override
    public void repeatDocPart(List<Object> contexts) {
        if (contexts == null) {
            contexts = Collections.emptyList();
        }

        CommentWrapper currentCommentWrapper = getCurrentCommentWrapper();
        ContentAccessor gcp = findGreatestCommonParent(
                currentCommentWrapper.getCommentRangeEnd().getParent(),
                (ContentAccessor) currentCommentWrapper.getCommentRangeStart().getParent()
        );
        List<Object> repeatElements = getRepeatElements(currentCommentWrapper, gcp);

        if (repeatElements.size() > 0) {
            try {
                subContexts.put(currentCommentWrapper, contexts);
                subTemplates.put(currentCommentWrapper, extractSubTemplate(currentCommentWrapper, repeatElements));
                gcpMap.put(currentCommentWrapper, gcp);
                repeatingElementsMap.put(currentCommentWrapper, repeatElements);
            } catch (InvalidFormatException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private WordprocessingMLPackage copyTemplate(WordprocessingMLPackage doc) throws Docx4JException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        doc.save(baos);
        return WordprocessingMLPackage.load(new ByteArrayInputStream(baos.toByteArray()));
    }

    @Override
    public void commitChanges(WordprocessingMLPackage document) {
        for (Map.Entry<CommentWrapper, List<Object>> entry : subContexts.entrySet()) {
            CommentWrapper commentWrapper = entry.getKey();
            List<Object> expressionContexts = entry.getValue();

            // index changes after each replacement, so we need to get the insert index at the last moment.
            List<Object> parentContent = gcpMap.get(commentWrapper).getContent();
            List<Object> repeatingElements = repeatingElementsMap.get(commentWrapper);
            int index = parentContent.indexOf(repeatingElements.get(0));

            if (expressionContexts == null) {
                if (config.isReplaceNullValues() && config.getNullValuesDefault() != null) {
                    P nullReplacedParagraph = ParagraphUtil.create(config.getNullValuesDefault());
                    parentContent.add(index, nullReplacedParagraph);
                    CommentUtil.deleteComment(commentWrapper);
                    parentContent.removeAll(repeatingElements);
                }
                continue;
            }

            for (Object subContext : expressionContexts) {
                try {
                    WordprocessingMLPackage subTemplate = copyTemplate(subTemplates.get(commentWrapper));
                    DocxStamper<Object> stamper = new DocxStamper<>(config);
                    ByteArrayOutputStream output = new ByteArrayOutputStream();
                    stamper.stamp(subTemplate, subContext, output);
                    WordprocessingMLPackage subDocument = WordprocessingMLPackage.load(new ByteArrayInputStream(output.toByteArray()));
                    try {
                        List<Object> changes = DocumentUtil.prepareDocumentForInsert(subDocument, document);
                        parentContent.addAll(index, changes);
                        index += changes.size();
                    } catch (Exception e) {
                        throw new RuntimeException("Unexpected error occured ! Skipping this comment", e);
                    }
                } catch (Docx4JException e) {
                    throw new RuntimeException(e);
                }
            }
            CommentUtil.deleteComment(commentWrapper);
            parentContent.removeAll(repeatingElements);
        }
    }

    @Override
    public void reset() {
        subContexts = new HashMap<>();
        subTemplates = new HashMap<>();
        gcpMap = new HashMap<>();
        repeatingElementsMap = new HashMap<>();
    }

    private WordprocessingMLPackage extractSubTemplate(CommentWrapper commentWrapper, List<Object> repeatElements) throws InvalidFormatException {
        CommentUtil.deleteComment(commentWrapper); // for deep copy without comment

        WordprocessingMLPackage document = WordprocessingMLPackage.createPackage();

        CommentsPart commentsPart = new CommentsPart();
        document.getMainDocumentPart().addTargetPart(commentsPart);

        document.getMainDocumentPart().getContent().addAll(repeatElements);

        Comments comments = objectFactory.createComments();
        commentWrapper.getChildren().forEach(comment -> comments.getComment().add(comment.getComment()));
        commentsPart.setContents(comments);

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
            return findInsertableParent(searchFrom);
        }
        return findGreatestCommonParent(targetSearch, (ContentAccessor) ((Child) searchFrom).getParent());
    }

    private static ContentAccessor findInsertableParent(ContentAccessor searchFrom) {
        if (!(searchFrom instanceof Tc || searchFrom instanceof Body)) {
            return findInsertableParent((ContentAccessor) ((Child) searchFrom).getParent());
        }
        return searchFrom;
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
