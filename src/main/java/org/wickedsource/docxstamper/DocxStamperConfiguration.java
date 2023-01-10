package org.wickedsource.docxstamper;

import org.wickedsource.docxstamper.api.EvaluationContextConfigurer;
import org.wickedsource.docxstamper.api.commentprocessor.ICommentProcessor;
import org.wickedsource.docxstamper.api.typeresolver.ITypeResolver;
import org.wickedsource.docxstamper.el.NoOpEvaluationContextConfigurer;
import org.wickedsource.docxstamper.replace.typeresolver.FallbackResolver;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides configuration parameters for DocxStamper.
 */
public class DocxStamperConfiguration {

    private String lineBreakPlaceholder;

    private EvaluationContextConfigurer evaluationContextConfigurer = new NoOpEvaluationContextConfigurer();

    private boolean failOnUnresolvedExpression = true;

    private boolean leaveEmptyOnExpressionError = false;

    private boolean replaceUnresolvedExpressions = false;

    private String unresolvedExpressionsDefaultValue = null;

    private boolean replaceNullValues = false;

    private String nullValuesDefault = null;

    private final Map<Class<?>, Object> commentProcessors = new HashMap<>();

    private final Map<Class<?>, ITypeResolver> typeResolvers = new HashMap<>();

    private ITypeResolver defaultTypeResolver = new FallbackResolver();

    private final Map<Class<?>, Object> expressionFunctions = new HashMap<>();

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
     *
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
     *
     * @param interfaceClass the interface whose methods should be exposed in the expression language.
     * @param implementation the implementation that should be called to evaluate invocations of the interface methods
     *                       within the expression language. Must implement the interface above.
     */
    public DocxStamperConfiguration exposeInterfaceToExpressionLanguage(Class<?> interfaceClass, Object implementation) {
        this.expressionFunctions.put(interfaceClass, implementation);
        return this;
    }

    /**
     * If an error is caught while evaluating an expression the expression will be replaced with an empty string instead
     * of leaving the original expression in the document.
     *
     * @param leaveEmpty true to replace expressions with empty string when an error is caught while evaluating
     */
    public DocxStamperConfiguration leaveEmptyOnExpressionError(boolean leaveEmpty) {
        this.leaveEmptyOnExpressionError = leaveEmpty;
        return this;
    }

    /**
     * Indicates if expressions that resolve to null should be processed.
     *
     * @param replaceNullValues true to replace null value expression with resolved value (which is null), false to leave the expression as is
     */
    public DocxStamperConfiguration replaceNullValues(boolean replaceNullValues) {
        this.replaceNullValues = replaceNullValues;
        return this;
    }

    /**
     * Indicates if expressions that resolve to null should be replaced by a global default value.
     *
     * @param nullValuesDefault value to use instead for expression resolving to null
     * @see DocxStamperConfiguration#replaceNullValues
     */
    public DocxStamperConfiguration nullValuesDefault(String nullValuesDefault) {
        this.nullValuesDefault = nullValuesDefault;
        return this;
    }

    /**
     * Indicates if expressions that doesn't resolve should be replaced by a default value.
     *
     * @param replaceUnresolvedExpressions true to replace null value expression with resolved value (which is null), false to leave the expression as is
     */
    public DocxStamperConfiguration replaceUnresolvedExpressions(boolean replaceUnresolvedExpressions) {
        this.replaceUnresolvedExpressions = replaceUnresolvedExpressions;
        return this;
    }

    /**
     * Indicates the default value to use for expressions that doesn't resolve.
     *
     * @param unresolvedExpressionsDefaultValue value to use instead for expression that doesn't resolve
     * @see DocxStamperConfiguration#replaceUnresolvedExpressions
     */
    public DocxStamperConfiguration unresolvedExpressionsDefaultValue(String unresolvedExpressionsDefaultValue) {
        this.unresolvedExpressionsDefaultValue = unresolvedExpressionsDefaultValue;
        return this;
    }

    /**
     * Creates a {@link DocxStamper} instance configured with this configuration.
     */
    public DocxStamper build() {
        return new DocxStamper(this);
    }

    public EvaluationContextConfigurer getEvaluationContextConfigurer() {
        return evaluationContextConfigurer;
    }

    public boolean isFailOnUnresolvedExpression() {
        return failOnUnresolvedExpression;
    }

    public Map<Class<?>, Object> getCommentProcessors() {
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

    public boolean isLeaveEmptyOnExpressionError() {
        return leaveEmptyOnExpressionError;
    }

    public boolean isReplaceNullValues() {
        return replaceNullValues;
    }

    public String getNullValuesDefault() {
        return nullValuesDefault;
    }

    public boolean isReplaceUnresolvedExpressions() {
        return replaceUnresolvedExpressions;
    }

    public String getUnresolvedExpressionsDefaultValue() {
        return unresolvedExpressionsDefaultValue;
    }

    public String getLineBreakPlaceholder() {
        return lineBreakPlaceholder;
    }

    public Map<Class<?>, Object> getExpressionFunctions() {
        return expressionFunctions;
    }
}
