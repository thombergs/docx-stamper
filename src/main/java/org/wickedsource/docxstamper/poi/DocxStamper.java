package org.wickedsource.docxstamper.poi;

import org.apache.poi.xwpf.usermodel.XWPFComment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTMarkupRange;
import org.wickedsource.docxstamper.DocxStamperException;
import org.wickedsource.docxstamper.el.ExpressionResolver;
import org.wickedsource.docxstamper.poi.commentprocessing.CommentProcessorRegistry;
import org.wickedsource.docxstamper.poi.commentprocessing.ContextFactory;
import org.wickedsource.docxstamper.poi.commentprocessing.displayif.DisplayIfProcessor;
import org.wickedsource.docxstamper.poi.commentprocessing.displayif.IDisplayIfProcessor;
import org.wickedsource.docxstamper.poi.coordinates.ParagraphCoordinates;
import org.wickedsource.docxstamper.poi.walk.DocumentWalker;
import org.wickedsource.docxstamper.poi.walk.ParagraphWalker;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class DocxStamper<T> {

    private ContextFactory<T> contextFactory = new ContextFactory<>();

    private PlaceholderReplacer<T> placeholderReplacer = new PlaceholderReplacer<>();

    private CommentStrippingXWPFDocumentWriter documentWriter = new CommentStrippingXWPFDocumentWriter();

    private CommentProcessorRegistry commentProcessorRegistry = new CommentProcessorRegistry();

    private ExpressionResolver expressionResolver = new ExpressionResolver();

    public DocxStamper() {
        commentProcessorRegistry.registerCommentProcessor(IDisplayIfProcessor.class, new DisplayIfProcessor());
    }

    public void stamp(InputStream template, T contextRoot, OutputStream out) throws IOException {
        try {
            T contextRootProxy = contextFactory.createProxy(contextRoot, commentProcessorRegistry);
            XWPFDocument document = new XWPFDocument(template);
            replaceExpressions(document, contextRootProxy);
            processComments(document, contextRootProxy);
            documentWriter.write(document, out);
        } catch (Exception e) {
            throw new DocxStamperException(e);
        }
    }

    private void replaceExpressions(XWPFDocument document, T contextRoot) {
        placeholderReplacer.resolveExpressions(document, contextRoot);
    }

    private void processComments(XWPFDocument document, final T contextRoot) {
        DocumentWalker walker = new ParagraphWalker(document) {
            @Override
            protected void onParagraph(ParagraphCoordinates paragraphCoordinates) {
                commentProcessorRegistry.setCurrentParagraphCoordinates(paragraphCoordinates);
                resolveExpressionsForParagraph(contextRoot, paragraphCoordinates.getParagraph());
            }
        };
        walker.walk();
        commentProcessorRegistry.commitChanges(document);
    }

    private void resolveExpressionsForParagraph(Object contextRoot, XWPFParagraph p) {
        List<CTMarkupRange> markupRangeList = p.getCTP().getCommentRangeStartList();
        for (CTMarkupRange anchor : markupRangeList) {
            XWPFComment comment = p.getDocument().getCommentByID(anchor.getId().toString());
            expressionResolver.resolveExpression(comment.getText(), contextRoot);
        }
    }

    public CommentProcessorRegistry getCommentProcessorRegistry() {
        return commentProcessorRegistry;
    }
}
