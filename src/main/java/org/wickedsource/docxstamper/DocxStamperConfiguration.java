package org.wickedsource.docxstamper;

import java.util.HashMap;
import java.util.Map;

import org.wickedsource.docxstamper.api.EvaluationContextConfigurer;
import org.wickedsource.docxstamper.api.commentprocessor.ICommentProcessor;
import org.wickedsource.docxstamper.api.typeresolver.ITypeResolver;
import org.wickedsource.docxstamper.el.NoOpEvaluationContextConfigurer;
import org.wickedsource.docxstamper.replace.typeresolver.FallbackResolver;

/**
 * Provides configuration parameters for DocxStamper.
 */
public class DocxStamperConfiguration {

  private String lineBreakPlaceholder;

  private EvaluationContextConfigurer evaluationContextConfigurer = new NoOpEvaluationContextConfigurer();

  private boolean failOnUnresolvedExpression = true;

  private Map<Class<?>, ICommentProcessor> commentProcessors = new HashMap<>();

  private Map<Class<?>, ITypeResolver> typeResolvers = new HashMap<>();

  private ITypeResolver defaultTypeResolver = new FallbackResolver();

  private Map<Class<?>, Object> expressionFunctions = new HashMap<>();

  /**
   * The String provided as lineBreakPlaceholder will be replaces with a line break
   * when stamping a document. If no lineBreakPlaceholder is provided, no replacement
   * will take place.
   *
   * @param lineBreakPlaceholder the String that should be replaced with line breaks during stamping.
   * @return the configuration object for chaining.
   */
  public DocxStamperConfiguration setLineBreakPlaceholder(String lineBreakPlaceholder) {
    this.lineBreakPlaceholder = lineBreakPlaceholder;
    return this;
  }

  /**
   * Provides an {@link EvaluationContextConfigurer} which may change the configuration of a Spring
   * {@link org.springframework.expression.EvaluationContext} which is used for evaluating expressions
   * in comments and text.
   * @param evaluationContextConfigurer the configurer to use.
   */
  public DocxStamperConfiguration setEvaluationContextConfigurer(EvaluationContextConfigurer evaluationContextConfigurer) {
    this.evaluationContextConfigurer = evaluationContextConfigurer;
    return this;
  }

  /**
   * If set to true, stamper will throw an {@link org.wickedsource.docxstamper.api.UnresolvedExpressionException}
   * if a variable expression or processor expression within the document or within the comments is encountered that cannot be resolved. Is set to true by default.
   */
  public DocxStamperConfiguration setFailOnUnresolvedExpression(boolean failOnUnresolvedExpression) {
    this.failOnUnresolvedExpression = failOnUnresolvedExpression;
    return this;
  }

  /**
   * Registers the specified ICommentProcessor as an implementation of the
   * specified interface.
   *
   * @param interfaceClass   the Interface which is implemented by the commentProcessor.
   * @param commentProcessor the commentProcessor implementing the specified interface.
   */
  public DocxStamperConfiguration addCommentProcessor(Class<?> interfaceClass,
                                                      ICommentProcessor commentProcessor) {
    this.commentProcessors.put(interfaceClass, commentProcessor);
    return this;
  }

  /**
   * <p>
   * Registers the given ITypeResolver for the given class. The registered ITypeResolver's resolve() method will only
   * be called with objects of the specified class.
   * </p>
   * <p>
   * Note that each type can only be resolved by ONE ITypeResolver implementation. Multiple calls to addTypeResolver()
   * with the same resolvedType parameter will override earlier calls.
   * </p>
   *
   * @param resolvedType the class whose objects are to be passed to the given ITypeResolver.
   * @param resolver     the resolver to resolve objects of the given type.
   * @param <T>          the type resolved by the ITypeResolver.
   */
  public <T> DocxStamperConfiguration addTypeResolver(Class<T> resolvedType, ITypeResolver resolver) {
    this.typeResolvers.put(resolvedType, resolver);
    return this;
  }

  /**
   * Exposes all methods of a given interface to the expression language.
   * @param interfaceClass the interface whose methods should be exposed in the expression language.
   * @param implementation  the implementation that should be called to evaluate invocations of the interface methods
   *                        within the expression language. Must implement the interface above.
   */
  public DocxStamperConfiguration exposeInterfaceToExpressionLanguage(Class<?> interfaceClass, Object implementation) {
    this.expressionFunctions.put(interfaceClass, implementation);
    return this;
  }

  /**
   * Creates a {@link DocxStamper} instance configured with this configuration.
   */
  public DocxStamper build() {
    return new DocxStamper(this);
  }

  EvaluationContextConfigurer getEvaluationContextConfigurer() {
    return evaluationContextConfigurer;
  }

  boolean isFailOnUnresolvedExpression() {
    return failOnUnresolvedExpression;
  }

  Map<Class<?>, ICommentProcessor> getCommentProcessors() {
    return commentProcessors;
  }

  Map<Class<?>, ITypeResolver> getTypeResolvers() {
    return typeResolvers;
  }

  ITypeResolver getDefaultTypeResolver() {
    return defaultTypeResolver;
  }

  public DocxStamperConfiguration setDefaultTypeResolver(ITypeResolver defaultTypeResolver) {
    this.defaultTypeResolver = defaultTypeResolver;
    return this;
  }

  String getLineBreakPlaceholder() {
    return lineBreakPlaceholder;
  }

  public Map<Class<?>, Object> getExpressionFunctions() {
    return expressionFunctions;
  }
}
