package org.wickedsource.docxstamper.replace;

import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Br;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.SpelParseException;
import org.wickedsource.docxstamper.api.DocxStamperException;
import org.wickedsource.docxstamper.api.typeresolver.ITypeResolver;
import org.wickedsource.docxstamper.api.typeresolver.TypeResolverRegistry;
import org.wickedsource.docxstamper.el.ExpressionResolver;
import org.wickedsource.docxstamper.el.ExpressionUtil;
import org.wickedsource.docxstamper.util.ParagraphWrapper;
import org.wickedsource.docxstamper.util.RunUtil;
import org.wickedsource.docxstamper.util.walk.BaseCoordinatesWalker;

import java.util.List;
import java.util.Optional;

/**
 * Replaces expressions in a document with the values provided by the {@link org.wickedsource.docxstamper.el.ExpressionResolver}.
 *
 * @author joseph
 * @version $Id: $Id
 */
public class PlaceholderReplacer {
    private static final Logger log = LoggerFactory.getLogger(PlaceholderReplacer.class);
    private final ExpressionResolver expressionResolver;
    private final TypeResolverRegistry typeResolverRegistry;
    private final boolean replaceNullValues;
    private final String nullValuesDefault;
    private final boolean failOnUnresolvedExpression;
    private final boolean leaveEmptyOnExpressionError;
    private final boolean replaceUnresolvedExpressions;
    private final String unresolvedExpressionsDefaultValue;
    private final String lineBreakPlaceholder;

    /**
     * <p>Constructor for PlaceholderReplacer.</p>
     *
     * @param typeResolverRegistry              the registry containing all available type resolvers.
     * @param expressionResolver                the expression resolver used to resolve expressions in the document.
     * @param replaceNullValues                 if set to true, null values are replaced with the value provided in the
     *                                          nullValuesDefault parameter.
     * @param nullValuesDefault                 the value to use when replacing null values.
     * @param failOnUnresolvedExpression        if set to true, an exception is thrown when an expression cannot be
     *                                          resolved.
     * @param replaceUnresolvedExpressions      if set to true, expressions that cannot be resolved are replaced by the
     *                                          value provided in the unresolvedExpressionsDefaultValue parameter.
     * @param unresolvedExpressionsDefaultValue the value to use when replacing unresolved expressions.
     * @param leaveEmptyOnExpressionError       if set to true, expressions that cannot be resolved are replaced by an
     *                                          empty string.
     * @param lineBreakPlaceholder              if set to a non-null value, all occurrences of this placeholder will be
     *                                          replaced with a line break.
     */
    public PlaceholderReplacer(
            TypeResolverRegistry typeResolverRegistry,
            ExpressionResolver expressionResolver,
            boolean replaceNullValues,
            String nullValuesDefault,
            boolean failOnUnresolvedExpression,
            boolean replaceUnresolvedExpressions,
            String unresolvedExpressionsDefaultValue,
            boolean leaveEmptyOnExpressionError,
            String lineBreakPlaceholder
    ) {
        this.typeResolverRegistry = typeResolverRegistry;
        this.expressionResolver = expressionResolver;
        this.replaceNullValues = replaceNullValues;
        this.nullValuesDefault = nullValuesDefault;
        this.failOnUnresolvedExpression = failOnUnresolvedExpression;
        this.replaceUnresolvedExpressions = replaceUnresolvedExpressions;
        this.unresolvedExpressionsDefaultValue = unresolvedExpressionsDefaultValue;
        this.leaveEmptyOnExpressionError = leaveEmptyOnExpressionError;
        this.lineBreakPlaceholder = lineBreakPlaceholder;
    }

    /**
     * Finds expressions in a document and resolves them against the specified context object. The expressions in the
     * document are then replaced by the resolved values.
     *
     * @param document          the document in which to replace all expressions.
     * @param expressionContext the context root
     */
    public void resolveExpressions(final WordprocessingMLPackage document, Object expressionContext) {
        new BaseCoordinatesWalker() {
            @Override
            protected void onParagraph(P paragraph) {
                resolveExpressionsForParagraph(paragraph, expressionContext, document);
            }
        }.walk(document);
    }

    /**
     * Finds expressions in the given paragraph and replaces them with the values provided by the expression resolver.
     *
     * @param p                 the paragraph in which to replace expressions.
     * @param expressionContext the context root
     * @param document          the document in which to replace all expressions.
     */
    @SuppressWarnings("unchecked")
    public void resolveExpressionsForParagraph(P p, Object expressionContext, WordprocessingMLPackage document) {
        ParagraphWrapper paragraphWrapper = new ParagraphWrapper(p);
        List<String> placeholders = ExpressionUtil.findVariableExpressions(paragraphWrapper.getText());
        for (String placeholder : placeholders) {
            try {
                Object replacement = expressionResolver.resolveExpression(placeholder, expressionContext);
                if (replacement != null) {
                    //noinspection rawtypes
                    ITypeResolver resolver = typeResolverRegistry.getResolverForType(replacement.getClass());
                    R replacementObject = resolver.resolve(document, replacement);
                    replace(paragraphWrapper, placeholder, replacementObject);
                    log.debug("Expression '{}' replaced by typeResolver {}", placeholder, resolver.getClass());
                } else if (replaceNullValues) {
                    //noinspection rawtypes
                    ITypeResolver resolver = typeResolverRegistry.getDefaultResolver();
                    R replacementObject = resolver.resolve(document, nullValuesDefault);
                    replace(paragraphWrapper, placeholder, replacementObject);
                    log.debug("Expression '{}' replaced by typeResolver {}", placeholder, resolver.getClass());
                }
            } catch (SpelEvaluationException | SpelParseException e) {
                if (isFailOnUnresolvedExpression()) {
                    String message = "Expression %s could not be resolved against context of type %s"
                            .formatted(placeholder, expressionContext.getClass());
                    throw new DocxStamperException(message, e);
                } else {
                    log.warn("Expression {} could not be resolved against context root of type {}. Reason: {}. Set log level to TRACE to view Stacktrace.",
                            placeholder,
                            expressionContext.getClass(),
                            e.getMessage());
                    log.trace("Reason for skipping expression:", e);
                    if (leaveEmptyOnExpressionError()) {
                        replace(paragraphWrapper, placeholder, "");
                    } else if (replaceUnresolvedExpressions()) {
                        replace(paragraphWrapper, placeholder, unresolvedExpressionsDefaultValue());
                    }
                }
            }
        }
        if (lineBreakPlaceholder() != null) {
            replaceLineBreaks(paragraphWrapper);
        }
    }

    private void replace(ParagraphWrapper p, String placeholder, R replacementRun) {
        p.replace(placeholder, replacementRun == null ? RunUtil.create("") : replacementRun);
    }

    private boolean isFailOnUnresolvedExpression() {
        return failOnUnresolvedExpression;
    }

    private boolean leaveEmptyOnExpressionError() {
        return leaveEmptyOnExpressionError;
    }

    /**
     * Replaces expressions in the given paragraph and replaces them with the values provided by the expression resolver.
     *
     * @param p                 the paragraph in which to replace expressions.
     * @param placeholder       the placeholder to replace.
     * @param replacementObject the object to replace the placeholder with.
     */
    public void replace(ParagraphWrapper p, String placeholder, String replacementObject) {
        Optional.ofNullable(replacementObject)
                .map(replacementStr -> RunUtil.create(replacementStr, p.getParagraph()))
                .ifPresent(replacementRun -> replace(p, placeholder, replacementRun));
    }

    private boolean replaceUnresolvedExpressions() {
        return replaceUnresolvedExpressions;
    }

    private String unresolvedExpressionsDefaultValue() {
        return unresolvedExpressionsDefaultValue;
    }

    private String lineBreakPlaceholder() {
        return lineBreakPlaceholder;
    }

    private void replaceLineBreaks(ParagraphWrapper paragraphWrapper) {
        Br lineBreak = Context.getWmlObjectFactory().createBr();
        R run = RunUtil.create(lineBreak);
        while (paragraphWrapper.getText().contains(lineBreakPlaceholder())) {
            replace(paragraphWrapper, lineBreakPlaceholder(), run);
        }
    }
}
