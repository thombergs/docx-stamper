package org.wickedsource.docxstamper.processor.repeat;

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.CommentsPart;
import org.docx4j.wml.*;
import org.jvnet.jaxb2_commons.ppp.Child;
import org.springframework.util.CollectionUtils;
import org.wickedsource.docxstamper.DocxStamper;
import org.wickedsource.docxstamper.DocxStamperConfiguration;
import org.wickedsource.docxstamper.api.typeresolver.TypeResolverRegistry;
import org.wickedsource.docxstamper.processor.BaseCommentProcessor;
import org.wickedsource.docxstamper.util.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.stream.Collectors;

public class RepeatDocPartProcessor extends BaseCommentProcessor implements IRepeatDocPartProcessor {
    private final Map<CommentWrapper, List<Object>> subContexts = new HashMap<>();
    private final Map<CommentWrapper, List<Object>> repeatElementsMap = new HashMap<>();
    private final Map<CommentWrapper, WordprocessingMLPackage> subTemplates = new HashMap<>();
    private final Map<CommentWrapper, ContentAccessor> gcpMap = new HashMap<>();
    /**
     * section break preceding the first repeating element if present
     */
    private final Map<CommentWrapper, SectPr> previousSectionBreak = new HashMap<>();
    /**
     * oddNumberOfBreaks will be set to true if repeating elements contain an odd number of section breaks,
     * false otherwise
     */
    private final Map<CommentWrapper, Boolean> oddNumberOfBreaks = new HashMap<>();

    private static final ObjectFactory objectFactory = Context.getWmlObjectFactory();

    public RepeatDocPartProcessor(
            DocxStamperConfiguration config,
            TypeResolverRegistry typeResolverRegistry
    ) {
        super(config, typeResolverRegistry);
    }


    @Override
    public void repeatDocPart(List<Object> contexts) throws Exception {
        if (contexts == null) {
            contexts = Collections.emptyList();
        }

        CommentWrapper currentCommentWrapper = getCurrentCommentWrapper();
        ContentAccessor gcp = findGreatestCommonParent(
                currentCommentWrapper.getCommentRangeEnd().getParent(),
                (ContentAccessor) currentCommentWrapper.getCommentRangeStart().getParent()
        );
        List<Object> repeatElements = getRepeatElements(currentCommentWrapper, gcp);

        if (!repeatElements.isEmpty()) {
            subTemplates.put(currentCommentWrapper, extractSubTemplate(currentCommentWrapper, repeatElements));
            subContexts.put(currentCommentWrapper, contexts);
            gcpMap.put(currentCommentWrapper, gcp);
            repeatElementsMap.put(currentCommentWrapper, repeatElements);
        }
    }

    private WordprocessingMLPackage copyTemplate(WordprocessingMLPackage doc) throws Docx4JException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        doc.save(baos);
        return WordprocessingMLPackage.load(new ByteArrayInputStream(baos.toByteArray()));
    }

    @Override
    public void commitChanges(WordprocessingMLPackage document) {
        for (CommentWrapper commentWrapper : subContexts.keySet()) {
            if (commentWrapper == null) throw new RuntimeException("commentWrapper should not be null");
            List<Object> expressionContexts = subContexts.get(commentWrapper);

            // index changes after each replacement, so we need to get the insert index at the last moment.
            ContentAccessor insertParentContentAccessor = Objects.requireNonNull(gcpMap.get(commentWrapper));
            List<Object> parentContent = insertParentContentAccessor.getContent();
            List<Object> repeatingElements = repeatElementsMap.get(commentWrapper);
            int index = parentContent.indexOf(repeatingElements.get(0));

            if (expressionContexts != null && !expressionContexts.isEmpty()) {
                Object lastExpressionContext = expressionContexts.get(expressionContexts.size() - 1);
            for (Object subContext : expressionContexts) {
                try {
                    WordprocessingMLPackage subTemplate = copyTemplate(subTemplates.get(commentWrapper));
                        DocxStamper<Object> stamper = new DocxStamper<>(configuration.copy());
                    ByteArrayOutputStream output = new ByteArrayOutputStream();
                    stamper.stamp(subTemplate, subContext, output);
                    WordprocessingMLPackage subDocument = WordprocessingMLPackage.load(new ByteArrayInputStream(output.toByteArray()));
                    try {
                        List<Object> changes = DocumentUtil.prepareDocumentForInsert(subDocument, document);
                        // make sure we replicate the previous section break before each repeated doc part
                        if (Objects.requireNonNull(oddNumberOfBreaks.get(commentWrapper)))
                            if (previousSectionBreak.get(commentWrapper) != null)
                                if (subContext != lastExpressionContext) {
                                    P lastP;
                                    if (changes.get(changes.size() - 1) instanceof P) {
                                        lastP = (P) changes.get(changes.size() - 1);
                                    } else {
                                        // when the last element to be repeated is not a paragraph, we need to add a new
                                        // one right after to carry the section break to have a valid xml
                                        lastP = objectFactory.createP();
                                        lastP.setParent(insertParentContentAccessor);
                                        changes.add(lastP);
                                    }

                                    SectionUtil.applySectionBreakToParagraph(previousSectionBreak.get(commentWrapper), lastP);
                                }
                        insertParentContentAccessor.getContent().addAll(index, changes);
                        index += changes.size();
                    } catch (Exception e) {
                        throw new RuntimeException("Unexpected error occured ! Skipping this comment", e);
                    }
                } catch (Docx4JException e) {
                    throw new RuntimeException(e);
                }
            }
            } else if (configuration.isReplaceNullValues() && configuration.getNullValuesDefault() != null) {
                P p = ParagraphUtil.create(configuration.getNullValuesDefault());
                p.setParent(insertParentContentAccessor);
                insertParentContentAccessor.getContent().add(index, p);
            }

            insertParentContentAccessor.getContent().removeAll(repeatElementsMap.get(commentWrapper));
        }
    }

    @Override
    public void reset() {
        subContexts.clear();
        subTemplates.clear();
        gcpMap.clear();
        repeatElementsMap.clear();
        previousSectionBreak.clear();
        oddNumberOfBreaks.clear();
    }

    private WordprocessingMLPackage extractSubTemplate(CommentWrapper commentWrapper, List<Object> repeatElements) throws Exception {
        WordprocessingMLPackage document = getDocument();
        WordprocessingMLPackage subDocument = WordprocessingMLPackage.createPackage();

        CommentsPart commentsPart = new CommentsPart();
        subDocument.getMainDocumentPart().addTargetPart(commentsPart);

        // copy the elements to repeat without comment range anchors
        List<Object> finalRepeatElements = repeatElements.stream().map(XmlUtils::deepCopy).collect(Collectors.toList());
        removeCommentAnchorsFromFinalElements(commentWrapper, finalRepeatElements);
        subDocument.getMainDocumentPart().getContent().addAll(finalRepeatElements);

        // copy the images from parent document using the original repeat elements
        ContentAccessor fakeBody = objectFactory.createBody();
        fakeBody.getContent().addAll(repeatElements);
        DocumentUtil.walkObjectsAndImportImages(fakeBody, document, subDocument);

        Comments comments = objectFactory.createComments();
        extractedSubComments(commentWrapper, comments);
        commentsPart.setContents(comments);

        return subDocument;
    }

    private void extractedSubComments(CommentWrapper commentWrapper, Comments comments) {
        for (CommentWrapper child : commentWrapper.getChildren()) {
            comments.getComment().add(child.getComment());
            if (CollectionUtils.isEmpty(child.getChildren())) {
                continue;
            }
            extractedSubComments(child, comments);
        }
    }

    private static void removeCommentAnchorsFromFinalElements(CommentWrapper commentWrapper, List<Object> finalRepeatElements) {
        ContentAccessor fakeBody = () -> finalRepeatElements;
        CommentUtil.deleteCommentFromElement(fakeBody, commentWrapper.getComment().getId());
    }

    private List<Object> getRepeatElements(CommentWrapper commentWrapper, ContentAccessor greatestCommonParent) {
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
        if (!repeatElements.isEmpty()) {
            previousSectionBreak.put(commentWrapper, SectionUtil.getPreviousSectionBreakIfPresent(repeatElements.get(0), greatestCommonParent));
            oddNumberOfBreaks.put(commentWrapper, SectionUtil.isOddNumberOfSectionBreaks(repeatElements));
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
