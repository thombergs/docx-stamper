package io.reflectoring.docxstamper;

import io.reflectoring.docxstamper.api.DocxStamperException;
import io.reflectoring.docxstamper.api.commentprocessor.ICommentProcessor;
import io.reflectoring.docxstamper.api.typeresolver.ITypeResolver;
import io.reflectoring.docxstamper.api.typeresolver.TypeResolverRegistry;
import io.reflectoring.docxstamper.el.ExpressionResolver;
import io.reflectoring.docxstamper.processor.CommentProcessorRegistry;
import io.reflectoring.docxstamper.processor.repeat.IParagraphRepeatProcessor;
import io.reflectoring.docxstamper.processor.repeat.IRepeatDocPartProcessor;
import io.reflectoring.docxstamper.processor.repeat.IRepeatProcessor;
import io.reflectoring.docxstamper.processor.repeat.ParagraphRepeatProcessor;
import io.reflectoring.docxstamper.processor.repeat.RepeatDocPartProcessor;
import io.reflectoring.docxstamper.processor.repeat.RepeatProcessor;
import io.reflectoring.docxstamper.proxy.ProxyBuilder;
import io.reflectoring.docxstamper.replace.PlaceholderReplacer;
import io.reflectoring.docxstamper.replace.typeresolver.DateResolver;
import io.reflectoring.docxstamper.replace.typeresolver.FallbackResolver;
import io.reflectoring.docxstamper.replace.typeresolver.image.Image;
import io.reflectoring.docxstamper.replace.typeresolver.image.ImageResolver;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import io.reflectoring.docxstamper.processor.displayif.DisplayIfProcessor;
import io.reflectoring.docxstamper.processor.displayif.IDisplayIfProcessor;
import io.reflectoring.docxstamper.processor.replaceExpression.IReplaceWithProcessor;
import io.reflectoring.docxstamper.processor.replaceExpression.ReplaceWithProcessor;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Map;

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
    for (Map.Entry<Class<?>, ITypeResolver> entry : config.getTypeResolvers().entrySet()) {
      typeResolverRegistry.registerTypeResolver(entry.getKey(), entry.getValue());
    }

    ExpressionResolver expressionResolver = new ExpressionResolver(config.getEvaluationContextConfigurer());
    placeholderReplacer = new PlaceholderReplacer<>(typeResolverRegistry, config.getLineBreakPlaceholder());
    placeholderReplacer.setExpressionResolver(expressionResolver);
    placeholderReplacer.setLeaveEmptyOnExpressionError(config.isLeaveEmptyOnExpressionError());
    placeholderReplacer.setReplaceNullValues(config.isReplaceNullValues());

    commentProcessorRegistry = new CommentProcessorRegistry(placeholderReplacer);
    commentProcessorRegistry.setExpressionResolver(expressionResolver);
    commentProcessorRegistry.setFailOnInvalidExpression(config.isFailOnUnresolvedExpression());
    commentProcessorRegistry.registerCommentProcessor(IRepeatProcessor.class, new RepeatProcessor(typeResolverRegistry, expressionResolver));
    commentProcessorRegistry.registerCommentProcessor(IParagraphRepeatProcessor.class, new ParagraphRepeatProcessor(typeResolverRegistry));
    commentProcessorRegistry.registerCommentProcessor(IRepeatDocPartProcessor.class, new RepeatDocPartProcessor(typeResolverRegistry));
    commentProcessorRegistry.registerCommentProcessor(IDisplayIfProcessor.class, new DisplayIfProcessor());
    commentProcessorRegistry.registerCommentProcessor(IReplaceWithProcessor.class,
            new ReplaceWithProcessor());
    for (Map.Entry<Class<?>, ICommentProcessor> entry : config.getCommentProcessors().entrySet()) {
      commentProcessorRegistry.registerCommentProcessor(entry.getKey(), entry.getValue());
    }
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
   * and register it via getCommentProcessorRegistry().addCommentProcessor().
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
      ProxyBuilder<T> proxyBuilder = addCustomInterfacesToContextRoot(contextRoot, this.config.getExpressionFunctions());
      processComments(document, proxyBuilder);
      replaceExpressions(document, proxyBuilder);
      document.save(out);
      commentProcessorRegistry.reset();
    } catch (DocxStamperException e) {
      throw e;
    } catch (Exception e) {
      throw new DocxStamperException(e);
    }
  }

  private ProxyBuilder<T> addCustomInterfacesToContextRoot(T contextRoot, Map<Class<?>, Object> interfacesToImplementations) {
    ProxyBuilder<T> proxyBuilder = new ProxyBuilder<T>()
            .withRoot(contextRoot);
    if (interfacesToImplementations.isEmpty()) {
      return proxyBuilder;
    }
    for (Map.Entry<Class<?>, Object> entry : interfacesToImplementations.entrySet()) {
      Class<?> interfaceClass = entry.getKey();
      Object implementation = entry.getValue();
      proxyBuilder.withInterface(interfaceClass, implementation);
    }
    return proxyBuilder;
  }

  private void replaceExpressions(WordprocessingMLPackage document, ProxyBuilder<T> proxyBuilder) {
    placeholderReplacer.resolveExpressions(document, proxyBuilder);
  }

  private void processComments(final WordprocessingMLPackage document, ProxyBuilder<T> proxyBuilder) {
    commentProcessorRegistry.runProcessors(document, proxyBuilder);
  }

}
