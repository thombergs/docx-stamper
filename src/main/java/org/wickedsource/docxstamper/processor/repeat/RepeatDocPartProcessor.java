package org.wickedsource.docxstamper.processor.repeat;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.CommentRangeEnd;
import org.docx4j.wml.CommentRangeStart;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.P;
import org.jvnet.jaxb2_commons.ppp.Child;
import org.wickedsource.docxstamper.api.typeresolver.TypeResolverRegistry;
import org.wickedsource.docxstamper.processor.BaseCommentProcessor;
import org.wickedsource.docxstamper.replace.PlaceholderReplacer;
import org.wickedsource.docxstamper.util.CommentWrapper;
import org.wickedsource.docxstamper.util.walk.BaseDocumentWalker;
import org.wickedsource.docxstamper.util.walk.DocumentWalker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RepeatDocPartProcessor extends BaseCommentProcessor implements IRepeatDocPartProcessor {

    private Map<CommentWrapper, DocPartToRepeat> partsToRepeat = new HashMap<>();
    private PlaceholderReplacer<Object> placeholderReplacer;

    public RepeatDocPartProcessor(TypeResolverRegistry typeResolverRegistry) {
        this.placeholderReplacer = new PlaceholderReplacer<>(typeResolverRegistry);
    }

    @Override
    public void repeatDocPart(List<Object> objects) {
        CommentWrapper commentWrapper = getCurrentCommentWrapper();
        List<Object> elements = getObjectsInsideComment(commentWrapper);

        partsToRepeat.put(commentWrapper, new DocPartToRepeat(objects, elements));
    }

    @Override
    public void commitChanges(WordprocessingMLPackage document) {
        for (CommentWrapper commentWrapper : partsToRepeat.keySet()) {
            DocPartToRepeat docPartToRepeat = partsToRepeat.get(commentWrapper);
            List<Object> expressionContexts = docPartToRepeat.getData();
            List<Object> elementsForRepeat = docPartToRepeat.getDocElements();

            ContentAccessor insertTarget = (ContentAccessor) commentWrapper.getCommentRangeEnd().getParent();
            int startInsertIndex = insertTarget.getContent().indexOf(commentWrapper.getCommentRangeEnd());

            for (final Object expressionContext : expressionContexts) {
                for (int j=0; j < elementsForRepeat.size(); j++) {
                    Object elClone = XmlUtils.deepCopy(elementsForRepeat.get(j));
                    if (XmlUtils.unwrap(elClone) instanceof ContentAccessor) {
                        DocumentWalker walker = new BaseDocumentWalker((ContentAccessor)elClone) {
                            @Override
                            protected void onParagraph(P paragraph) {
                                placeholderReplacer.resolveExpressionsForParagraph(paragraph, expressionContext, document);
                            }
                        };
                        walker.walk();
                    }
                    insertTarget.getContent().add(startInsertIndex + j, elClone);
                }
            }

            for (Object element : elementsForRepeat) {
                ContentAccessor elementParent = (ContentAccessor) ((Child) element).getParent();
                for (int i=0; i < elementParent.getContent().size(); i++) {
                    Object unwrapedObject = XmlUtils.unwrap(elementParent.getContent().get(i));
                    if (element.equals(unwrapedObject)) {
                        elementParent.getContent().remove(i);
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void reset() {
        partsToRepeat = new HashMap<>();
    }

    private static List<Object> getObjectsInsideComment(CommentWrapper commentWrapper) {
        CommentRangeStart start = commentWrapper.getCommentRangeStart();
        ContentAccessor parent = (ContentAccessor) start.getParent();
        return recursiveFindCommentElements(commentWrapper.getCommentRangeEnd(), parent, parent.getContent().indexOf(start));
    }

    private static List<Object> recursiveFindCommentElements(CommentRangeEnd commentRangeEnd, ContentAccessor contentAccessor, int lastCheckedIndex) {
        List<Object> elements = new ArrayList<>();
        for (int i = lastCheckedIndex + 1; i < contentAccessor.getContent().size(); i++) {
            Object object = XmlUtils.unwrap(contentAccessor.getContent().get(i));
            if (commentRangeEnd.equals(object)
                || (object instanceof ContentAccessor
                    && findCommentEndInsideContent(commentRangeEnd, (ContentAccessor) object))) {
                return elements;
            }
            elements.add(object);
        }
        if (contentAccessor instanceof Child) {
            Object parent = ((Child)contentAccessor).getParent();
            if (parent instanceof ContentAccessor) {
                ContentAccessor parentContentAccessor = (ContentAccessor) parent;
                elements = recursiveFindCommentElements(commentRangeEnd, parentContentAccessor,
                        parentContentAccessor.getContent().indexOf(contentAccessor));
                elements.add(0, contentAccessor);
                return elements;
            }
        }
        return elements;
    }

    private static boolean findCommentEndInsideContent(CommentRangeEnd searchTarget, ContentAccessor content) {
        for (Object object : content.getContent()) {
            if (searchTarget.equals(object)) {
                return true;
            } else if (object instanceof ContentAccessor
                    && findCommentEndInsideContent(searchTarget, (ContentAccessor) object)) {
                return true;
            }
        }
        return false;
    }

    private static class DocPartToRepeat {
        private List<Object> data;
        private List<Object> docElements;

        DocPartToRepeat(List<Object> data, List<Object> docElements) {
            this.data = data;
            this.docElements = docElements;
        }

        List<Object> getData() {
            return data;
        }

        List<Object> getDocElements() {
            return docElements;
        }
    }
}
