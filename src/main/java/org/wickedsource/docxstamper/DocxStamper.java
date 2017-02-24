package org.wickedsource.docxstamper;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.wickedsource.docxstamper.api.DocxStamperException;
import org.wickedsource.docxstamper.api.commentprocessor.CommentProcessorRegistry;
import org.wickedsource.docxstamper.api.typeresolver.TypeResolverRegistry;
import org.wickedsource.docxstamper.processor.displayif.DisplayIfProcessor;
import org.wickedsource.docxstamper.processor.displayif.IDisplayIfProcessor;
import org.wickedsource.docxstamper.processor.repeat.IRepeatProcessor;
import org.wickedsource.docxstamper.processor.repeat.RepeatProcessor;
import org.wickedsource.docxstamper.processor.replaceExpression.IReplaceExpressionProcessor;
import org.wickedsource.docxstamper.processor.replaceExpression.ReplaceExpressionProcessor;
import org.wickedsource.docxstamper.replace.PlaceholderReplacer;
import org.wickedsource.docxstamper.replace.typeresolver.DateResolver;
import org.wickedsource.docxstamper.replace.typeresolver.FallbackResolver;
import org.wickedsource.docxstamper.replace.typeresolver.image.Image;
import org.wickedsource.docxstamper.replace.typeresolver.image.ImageResolver;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

/**
 * <p>
 * Main class of the docx-stamper library. This class can be used to "stamp" .docx templates
 * to create a .docx document filled with custom data at runtime.
 * </p>
 *
 * @param <T> the class of the context object used to resolve expressions against.
 */
public class DocxStamper<T> {

    private PlaceholderReplacer<T> placeholderReplacer;

    private CommentProcessorRegistry commentProcessorRegistry;

    private TypeResolverRegistry typeResolverRegistry;

    private DocxStamperConfiguration config = new DocxStamperConfiguration();

    public DocxStamper() {
        initFields();
    }

    public DocxStamper(DocxStamperConfiguration config) {
        this.config = config;
        initFields();
    }

    private void initFields() {
        typeResolverRegistry = new TypeResolverRegistry(new FallbackResolver());
        typeResolverRegistry.registerTypeResolver(Image.class, new ImageResolver());
        typeResolverRegistry.registerTypeResolver(Date.class, new DateResolver("dd.MM.yyyy"));
        placeholderReplacer = new PlaceholderReplacer<>(typeResolverRegistry, config.getLineBreakPlaceholder());
        commentProcessorRegistry = new CommentProcessorRegistry(placeholderReplacer);
        commentProcessorRegistry.setFailOnInvalidExpression(true);
        commentProcessorRegistry.registerCommentProcessor(IRepeatProcessor.class, new RepeatProcessor(typeResolverRegistry));
        commentProcessorRegistry.registerCommentProcessor(IDisplayIfProcessor.class, new DisplayIfProcessor());
        commentProcessorRegistry.registerCommentProcessor(IReplaceExpressionProcessor.class, new ReplaceExpressionProcessor());
    }

    /**
     * <p>
     * Reads in a .docx template and "stamps" it into the given OutputStream, using the specified context object to
     * fill out any expressions it finds.
     * </p>
     * <p>
     * In the .docx template you have the following options to influence the "stamping" process:
     * </p>
     * <ul>
     * <li>Use expressions like ${name} or ${person.isOlderThan(18)} in the template's text. These expressions are resolved
     * against the contextRoot object you pass into this method and are replaced by the results.</li>
     * <li>Use comments within the .docx template to mark certain paragraphs to be manipulated. </li>
     * </ul>
     * <p>
     * Within comments, you can put expressions in which you can use the following methods by default:
     * </p>
     * <ul>
     * <li><em>displayParagraphIf(boolean)</em> to conditionally display paragraphs or not</li>
     * <li><em>displayTableRowIf(boolean)</em> to conditionally display table rows or not</li>
     * <li><em>displayTableIf(boolean)</em> to conditionally display whole tables or not</li>
     * <li><em>repeatTableRow(List&lt;Object&gt;)</em> to create a new table row for each object in the list and resolve expressions
     * within the table cells against one of the objects within the list.</li>
     * </ul>
     * <p>
     * If you need a wider vocabulary of methods available in the comments, you can create your own ICommentProcessor
     * and register it via getCommentProcessorRegistry().registerCommentProcessor().
     * </p>
     *
     * @param template    the .docx template.
     * @param contextRoot the context root object against which all expressions found in the template are evaluated.
     * @param out         the output stream in which to write the resulting .docx document.
     * @throws DocxStamperException in case of an error.
     */
    public void stamp(InputStream template, T contextRoot, OutputStream out) throws DocxStamperException {
        try {
            WordprocessingMLPackage document = WordprocessingMLPackage.load(template);
            stamp(document, contextRoot, out);
        } catch (DocxStamperException e) {
            throw e;
        } catch (Exception e) {
            throw new DocxStamperException(e);
        }
    }

    /**
     * Same as stamp(InputStream, T, OutputStream) except that you may pass in a DOCX4J document as a template instead
     * of an InputStream.
     *
     * @param document    the .docx template.
     * @param contextRoot the context root object against which all expressions found in the template are evaluated.
     * @param out         the output stream in which to write the resulting .docx document.
     * @throws DocxStamperException in case of an error.
     */
    public void stamp(WordprocessingMLPackage document, T contextRoot, OutputStream out) throws DocxStamperException {
        try {
            replaceExpressions(document, contextRoot);
            processComments(document, contextRoot);
            document.save(out);
        } catch (DocxStamperException e) {
            throw e;
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

    /**
     * Returns the registry in which all ITypeResolvers are registered. Use it to register your own ITypeResolver
     * implementation.
     */
    public TypeResolverRegistry getTypeResolverRegistry() {
        return typeResolverRegistry;
    }

    /**
     * Returns the registry in which all ICommentProcessors are registeres. Use it to register your own
     * ICommentProcessor implementation.
     */
    public CommentProcessorRegistry getCommentProcessorRegistry() {
        return commentProcessorRegistry;
    }

    /**
     * If set to true, calling {@link #stamp(InputStream, Object, OutputStream)} will throw an {@link org.wickedsource.docxstamper.api.UnresolvedExpressionException}
     * if a variable expression or processor expression within the document or within the comments is encountered that cannot be resolved. Is set to true by default.
     */
    public void setFailOnUnresolvedExpression(boolean failOnUnresolvedExpression) {
        commentProcessorRegistry.setFailOnInvalidExpression(failOnUnresolvedExpression);
    }

}
