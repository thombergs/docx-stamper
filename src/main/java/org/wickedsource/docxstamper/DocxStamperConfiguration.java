package org.wickedsource.docxstamper;

import org.springframework.expression.spel.SpelParserConfiguration;
import org.wickedsource.docxstamper.api.EvaluationContextConfigurer;
import org.wickedsource.docxstamper.api.preprocessor.PreProcessor;
import org.wickedsource.docxstamper.api.typeresolver.ITypeResolver;
import org.wickedsource.docxstamper.el.DefaultEvaluationContextConfigurer;
import org.wickedsource.docxstamper.processor.CommentProcessorFactory;
import org.wickedsource.docxstamper.processor.displayif.IDisplayIfProcessor;
import org.wickedsource.docxstamper.processor.repeat.IParagraphRepeatProcessor;
import org.wickedsource.docxstamper.processor.repeat.IRepeatDocPartProcessor;
import org.wickedsource.docxstamper.processor.repeat.IRepeatProcessor;
import org.wickedsource.docxstamper.processor.replaceExpression.IReplaceWithProcessor;
import org.wickedsource.docxstamper.processor.table.ITableResolver;
import org.wickedsource.docxstamper.replace.typeresolver.FallbackResolver;

import java.util.*;

/**
 * Provides configuration parameters for DocxStamper.
 *
 * @author joseph
 * @version $Id: $Id
 */
public class DocxStamperConfiguration {

    private final Map<Class<?>, CommentProcessorBuilder> commentProcessors = new HashMap<>();
    private final Map<Class<?>, ITypeResolver<?>> typeResolvers = new HashMap<>();
    private final Map<Class<?>, Object> expressionFunctions = new HashMap<>();
    private final List<PreProcessor> preprocessors = new ArrayList<>();
    private String lineBreakPlaceholder;
    private EvaluationContextConfigurer evaluationContextConfigurer = new DefaultEvaluationContextConfigurer();
    private boolean failOnUnresolvedExpression = true;
    private boolean leaveEmptyOnExpressionError = false;
    private boolean replaceUnresolvedExpressions = false;
    private String unresolvedExpressionsDefaultValue = null;
    private boolean replaceNullValues = false;
    private String nullValuesDefault = null;
    private ITypeResolver<Object> defaultTypeResolver = new FallbackResolver();
    private SpelParserConfiguration spelParserConfiguration = new SpelParserConfiguration();

    /**
     * Creates a new configuration with default values.
     */
    public DocxStamperConfiguration() {
        CommentProcessorFactory pf = new CommentProcessorFactory(this);
        commentProcessors.put(IRepeatProcessor.class, pf::repeat);
        commentProcessors.put(IParagraphRepeatProcessor.class, pf::repeatParagraph);
        commentProcessors.put(IRepeatDocPartProcessor.class, pf::repeatDocPart);
        commentProcessors.put(ITableResolver.class, pf::tableResolver);
        commentProcessors.put(IDisplayIfProcessor.class, pf::displayIf);
        commentProcessors.put(IReplaceWithProcessor.class, pf::replaceWith);
    }

    /**
     * <p>nullReplacementValue.</p>
     *
     * @return the {@link org.wickedsource.docxstamper.api.typeresolver.ITypeResolver} for the given type or the default resolver if no resolver is registered for the given type.
     */
    public Optional<String> nullReplacementValue() {
        return replaceNullValues
                ? Optional.ofNullable(nullValuesDefault)
                : Optional.empty();
    }

    /**
     * <p>isFailOnUnresolvedExpression.</p>
     *
     * @return a boolean
     */
    public boolean isFailOnUnresolvedExpression() {
        return failOnUnresolvedExpression;
    }

    /**
     * The String provided as lineBreakPlaceholder will be replaced with a line break
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
     * Provides an {@link org.wickedsource.docxstamper.api.EvaluationContextConfigurer} which may change the configuration of a Spring
     * {@link org.springframework.expression.EvaluationContext} which is used for evaluating expressions
     * in comments and text.
     *
     * @param evaluationContextConfigurer the configurer to use.
     * @return a {@link org.wickedsource.docxstamper.DocxStamperConfiguration} object
     */
    public DocxStamperConfiguration setEvaluationContextConfigurer(EvaluationContextConfigurer evaluationContextConfigurer) {
        this.evaluationContextConfigurer = evaluationContextConfigurer;
        return this;
    }

    /**
     * Indicates if expressions that resolve to null should be replaced by a global default value.
     *
     * @param nullValuesDefault value to use instead for expression resolving to null
     * @return a {@link org.wickedsource.docxstamper.DocxStamperConfiguration} object
     * @see DocxStamperConfiguration#replaceNullValues
     */
    public DocxStamperConfiguration nullValuesDefault(String nullValuesDefault) {
        this.nullValuesDefault = nullValuesDefault;
        return this;
    }

    /**
     * Indicates if expressions that resolve to null should be processed.
     *
     * @param replaceNullValues true to replace null value expression with resolved value (which is null), false to leave the expression as is
     * @return a {@link org.wickedsource.docxstamper.DocxStamperConfiguration} object
     */
    public DocxStamperConfiguration replaceNullValues(boolean replaceNullValues) {
        this.replaceNullValues = replaceNullValues;
        return this;
    }

    /**
     * Indicates the default value to use for expressions that doesn't resolve.
     *
     * @param unresolvedExpressionsDefaultValue value to use instead for expression that doesn't resolve
     * @return a {@link org.wickedsource.docxstamper.DocxStamperConfiguration} object
     * @see DocxStamperConfiguration#replaceUnresolvedExpressions
     */
    public DocxStamperConfiguration unresolvedExpressionsDefaultValue(String unresolvedExpressionsDefaultValue) {
        this.unresolvedExpressionsDefaultValue = unresolvedExpressionsDefaultValue;
        return this;
    }

    /**
     * Indicates if expressions that doesn't resolve should be replaced by a default value.
     *
     * @param replaceUnresolvedExpressions true to replace null value expression with resolved value (which is null), false to leave the expression as is
     * @return a {@link org.wickedsource.docxstamper.DocxStamperConfiguration} object
     */
    public DocxStamperConfiguration replaceUnresolvedExpressions(boolean replaceUnresolvedExpressions) {
        this.replaceUnresolvedExpressions = replaceUnresolvedExpressions;
        return this;
    }

    /**
     * If an error is caught while evaluating an expression the expression will be replaced with an empty string instead
     * of leaving the original expression in the document.
     *
     * @param leaveEmpty true to replace expressions with empty string when an error is caught while evaluating
     * @return a {@link org.wickedsource.docxstamper.DocxStamperConfiguration} object
     */
    public DocxStamperConfiguration leaveEmptyOnExpressionError(boolean leaveEmpty) {
        this.leaveEmptyOnExpressionError = leaveEmpty;
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
     * @return a {@link org.wickedsource.docxstamper.DocxStamperConfiguration} object
     */
    public <T> DocxStamperConfiguration addTypeResolver(Class<T> resolvedType, ITypeResolver<T> resolver) {
        this.typeResolvers.put(resolvedType, resolver);
        return this;
    }

    /**
     * Exposes all methods of a given interface to the expression language.
     *
     * @param interfaceClass the interface whose methods should be exposed in the expression language.
     * @param implementation the implementation that should be called to evaluate invocations of the interface methods
     *                       within the expression language. Must implement the interface above.
     * @return a {@link org.wickedsource.docxstamper.DocxStamperConfiguration} object
     */
    public DocxStamperConfiguration exposeInterfaceToExpressionLanguage(Class<?> interfaceClass, Object implementation) {
        this.expressionFunctions.put(interfaceClass, implementation);
        return this;
    }

    /**
     * Registers the specified ICommentProcessor as an implementation of the
     * specified interface.
     *
     * @param interfaceClass          the Interface which is implemented by the commentProcessor.
     * @param commentProcessorFactory the commentProcessor factory generating the specified interface.
     * @return a {@link org.wickedsource.docxstamper.DocxStamperConfiguration} object
     */
    public DocxStamperConfiguration addCommentProcessor(
            Class<?> interfaceClass,
            CommentProcessorBuilder commentProcessorFactory
    ) {
        this.commentProcessors.put(interfaceClass, commentProcessorFactory);
        return this;
    }

    /**
     * Creates a {@link org.wickedsource.docxstamper.DocxStamper} instance configured with this configuration.
     *
     * @param <T> a T class
     * @return a {@link org.wickedsource.docxstamper.DocxStamper} object
     * @deprecated use new DocxStamper(DocxStamperConfiguration configuration) instead
     */
    @Deprecated(forRemoval = true, since = "1.6.4")
    public <T> DocxStamper<T> build() {
        return new DocxStamper<>(this);
    }

    /**
     * <p>Setter for the field <code>defaultTypeResolver</code>.</p>
     *
     * @param defaultTypeResolver a {@link org.wickedsource.docxstamper.api.typeresolver.ITypeResolver} object
     * @return a {@link org.wickedsource.docxstamper.DocxStamperConfiguration} object
     */
    public DocxStamperConfiguration setDefaultTypeResolver(ITypeResolver<? super Object> defaultTypeResolver) {
        this.defaultTypeResolver = defaultTypeResolver;
        return this;
    }

    /**
     * Adds a preprocessor to the configuration.
     *
     * @param preprocessor the preprocessor to add.
     */
    public void addPreprocessor(PreProcessor preprocessor) {
        preprocessors.add(preprocessor);
    }

    /**
     * Sets the {@link org.springframework.expression.spel.SpelParserConfiguration} to use for expression parsing.
     * <p>
     * Note that this configuration will be used for all expressions in the document, including expressions in comments!
     * </p>
     *
     * @param spelParserConfiguration the configuration to use.
     * @return a {@link org.wickedsource.docxstamper.DocxStamperConfiguration} object
     */
    public DocxStamperConfiguration setSpelParserConfiguration(SpelParserConfiguration spelParserConfiguration) {
        this.spelParserConfiguration = spelParserConfiguration;
        return this;
    }

    /**
     * If set to true, stamper will throw an {@link org.wickedsource.docxstamper.api.DocxStamperException}
     * if a variable expression or processor expression within the document or within the comments is encountered that cannot be resolved. Is set to true by default.
     *
     * @param failOnUnresolvedExpression a boolean
     * @return a {@link org.wickedsource.docxstamper.DocxStamperConfiguration} object
     */
    public DocxStamperConfiguration setFailOnUnresolvedExpression(boolean failOnUnresolvedExpression) {
        this.failOnUnresolvedExpression = failOnUnresolvedExpression;
        return this;
    }

    /**
     * <p>isReplaceUnresolvedExpressions.</p>
     *
     * @return a boolean
     */
    public boolean isReplaceUnresolvedExpressions() {
        return replaceUnresolvedExpressions;
    }

    /**
     * <p>isLeaveEmptyOnExpressionError.</p>
     *
     * @return a boolean
     */
    public boolean isLeaveEmptyOnExpressionError() {
        return leaveEmptyOnExpressionError;
    }

    /**
     * <p>Getter for the field <code>unresolvedExpressionsDefaultValue</code>.</p>
     *
     * @return a {@link java.lang.String} object
     */
    public String getUnresolvedExpressionsDefaultValue() {
        return unresolvedExpressionsDefaultValue;
    }

    /**
     * <p>Getter for the field <code>lineBreakPlaceholder</code>.</p>
     *
     * @return a {@link java.lang.String} object
     */
    public String getLineBreakPlaceholder() {
        return lineBreakPlaceholder;
    }

    /**
     * <p>Getter for the field <code>evaluationContextConfigurer</code>.</p>
     *
     * @return a {@link org.wickedsource.docxstamper.api.EvaluationContextConfigurer} object
     */
    public EvaluationContextConfigurer getEvaluationContextConfigurer() {
        return evaluationContextConfigurer;
    }

    /**
     * <p>Getter for the field <code>spelParserConfiguration</code>.</p>
     *
     * @return a {@link org.springframework.expression.spel.SpelParserConfiguration} object
     */
    public SpelParserConfiguration getSpelParserConfiguration() {
        return spelParserConfiguration;
    }

    /**
     * <p>Getter for the field <code>expressionFunctions</code>.</p>
     *
     * @return a {@link java.util.Map} object
     */
    public Map<Class<?>, Object> getExpressionFunctions() {
        return expressionFunctions;
    }

    /**
     * <p>Getter for the field <code>typeResolvers</code>.</p>
     *
     * @return a {@link java.util.Map} object
     */
    public Map<Class<?>, ITypeResolver<?>> getTypeResolvers() {
        return typeResolvers;
    }

    /**
     * <p>Getter for the field <code>defaultTypeResolver</code>.</p>
     *
     * @return a {@link org.wickedsource.docxstamper.api.typeresolver.ITypeResolver} object
     */
    public ITypeResolver<Object> getDefaultTypeResolver() {
        return defaultTypeResolver;
    }

    /**
     * <p>Getter for the field <code>commentProcessors</code>.</p>
     *
     * @return a {@link java.util.Map} object
     */
    public Map<Class<?>, CommentProcessorBuilder> getCommentProcessors() {
        return commentProcessors;
    }

    /**
     * <p>isReplaceNullValues.</p>
     *
     * @return a boolean
     */
    public boolean isReplaceNullValues() {
        return replaceNullValues;
    }

    /**
     * <p>Getter for the field <code>nullValuesDefault</code>.</p>
     *
     * @return a {@link java.lang.String} object
     */
    public String getNullValuesDefault() {
        return nullValuesDefault;
    }

    /**
     * <p>Getter for the field <code>preprocessors</code>.</p>
     *
     * @return a {@link java.util.List} object
     */
    public List<PreProcessor> getPreprocessors() {
        return preprocessors;
    }
}
