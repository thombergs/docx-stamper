package org.wickedsource.docxstamper.processor.repeat;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.docx4j.wml.Tr;
import org.wickedsource.docxstamper.replace.PlaceholderReplacer;
import org.wickedsource.docxstamper.util.walk.BaseDocumentWalker;

class ParagraphResolverDocumentWalker extends BaseDocumentWalker {
    private final Object expressionContext;
    private final WordprocessingMLPackage document;
    private final PlaceholderReplacer placeholderReplacer;

    public ParagraphResolverDocumentWalker(Tr rowClone, Object expressionContext, WordprocessingMLPackage document, PlaceholderReplacer replacer) {
        super(rowClone);
        this.expressionContext = expressionContext;
        this.document = document;
        this.placeholderReplacer = replacer;
    }

    @Override
    protected void onParagraph(P paragraph) {
        placeholderReplacer.resolveExpressionsForParagraph(paragraph, expressionContext, document);
    }
}
