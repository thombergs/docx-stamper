package org.wickedsource.docxstamper;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.wickedsource.docxstamper.processor.CommentProcessorRegistry;
import org.wickedsource.docxstamper.processor.displayif.DisplayIfProcessor;
import org.wickedsource.docxstamper.processor.displayif.IDisplayIfProcessor;
import org.wickedsource.docxstamper.processor.repeat.IRepeatProcessor;
import org.wickedsource.docxstamper.processor.repeat.RepeatProcessor;
import org.wickedsource.docxstamper.replace.PlaceholderReplacer;
import org.wickedsource.docxstamper.replace.TypeResolverRegistry;
import org.wickedsource.docxstamper.replace.image.Image;
import org.wickedsource.docxstamper.replace.image.ImageResolver;
import org.wickedsource.docxstamper.replace.string.StringResolver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DocxStamper<T> {

    private PlaceholderReplacer<T> placeholderReplacer;

    private CommentProcessorRegistry commentProcessorRegistry = new CommentProcessorRegistry();

    private TypeResolverRegistry typeResolverRegistry;

    public DocxStamper() {
        typeResolverRegistry = new TypeResolverRegistry(new StringResolver());
        typeResolverRegistry.registerTypeResolver(Image.class, new ImageResolver());
        placeholderReplacer = new PlaceholderReplacer<>(typeResolverRegistry);
        commentProcessorRegistry.registerCommentProcessor(IRepeatProcessor.class, new RepeatProcessor(typeResolverRegistry));
        commentProcessorRegistry.registerCommentProcessor(IDisplayIfProcessor.class, new DisplayIfProcessor());
    }

    public void stamp(InputStream template, T contextRoot, OutputStream out) throws IOException, Docx4JException {
        WordprocessingMLPackage document = WordprocessingMLPackage.load(template);
        stamp(document, contextRoot, out);
    }

    public void stamp(WordprocessingMLPackage document, T contextRoot, OutputStream out) {
        try {
            replaceExpressions(document, contextRoot);
            processComments(document, contextRoot);
            document.save(out);
        } catch (Exception e) {
            throw new DocxStamperException(e);
        }
    }

    private void replaceExpressions(WordprocessingMLPackage document, T contextRoot) {
        placeholderReplacer.resolveExpressions(document, contextRoot);
    }

    private void processComments(final WordprocessingMLPackage document, final T contextRoot) {
        commentProcessorRegistry.runProcessors(document, contextRoot);
    }

    public TypeResolverRegistry getTypeResolverRegistry() {
        return typeResolverRegistry;
    }

    public CommentProcessorRegistry getCommentProcessorRegistry() {
        return commentProcessorRegistry;
    }
}
