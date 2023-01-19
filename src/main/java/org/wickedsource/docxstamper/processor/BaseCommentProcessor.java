package org.wickedsource.docxstamper.processor;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.wickedsource.docxstamper.DocxStamperConfiguration;
import org.wickedsource.docxstamper.api.commentprocessor.ICommentProcessor;
import org.wickedsource.docxstamper.api.coordinates.ParagraphCoordinates;
import org.wickedsource.docxstamper.api.coordinates.RunCoordinates;
import org.wickedsource.docxstamper.api.typeresolver.TypeResolverRegistry;
import org.wickedsource.docxstamper.replace.PlaceholderReplacer;
import org.wickedsource.docxstamper.util.CommentWrapper;

import java.util.Objects;

public abstract class BaseCommentProcessor implements ICommentProcessor {
    protected final TypeResolverRegistry typeResolverRegistry;

    protected final DocxStamperConfiguration configuration;
    protected final PlaceholderReplacer placeholderReplacer;
    private ParagraphCoordinates currentParagraphCoordinates;

    private RunCoordinates currentRunCoordinates;

    private CommentWrapper currentCommentWrapper;

    private WordprocessingMLPackage document;

    public BaseCommentProcessor(DocxStamperConfiguration config, TypeResolverRegistry typeResolverRegistry) {
        this.configuration = config;
        this.typeResolverRegistry = typeResolverRegistry;
        this.placeholderReplacer = new PlaceholderReplacer(typeResolverRegistry, configuration);
    }

    public RunCoordinates getCurrentRunCoordinates() {
        return currentRunCoordinates;
    }

    @Override
    public void setCurrentRunCoordinates(RunCoordinates currentRunCoordinates) {
        this.currentRunCoordinates = currentRunCoordinates;
    }

    @Override
    public void setCurrentParagraphCoordinates(ParagraphCoordinates coordinates) {
        this.currentParagraphCoordinates = coordinates;
    }

    public ParagraphCoordinates getCurrentParagraphCoordinates() {
        return currentParagraphCoordinates;
    }

    @Override
    public void setCurrentCommentWrapper(CommentWrapper currentCommentWrapper) {
        Objects.requireNonNull(currentCommentWrapper.getCommentRangeStart());
        Objects.requireNonNull(currentCommentWrapper.getCommentRangeEnd());
        this.currentCommentWrapper = currentCommentWrapper;
    }

    public CommentWrapper getCurrentCommentWrapper() {
        return currentCommentWrapper;
    }

    @Override
    public void setDocument(WordprocessingMLPackage document) {
        this.document = document;
    }

    public WordprocessingMLPackage getDocument() {
        return document;
    }
}
