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
import org.wickedsource.docxstamper.api.typeresolver.ITypeResolver;
import org.wickedsource.docxstamper.api.typeresolver.TypeResolverRegistry;
import org.wickedsource.docxstamper.el.ExpressionResolver;
import org.wickedsource.docxstamper.el.ExpressionUtil;
import org.wickedsource.docxstamper.util.RunUtil;
import org.wickedsource.docxstamper.walk.coordinates.BaseCoordinatesWalker;
import org.wickedsource.docxstamper.walk.coordinates.CoordinatesWalker;
import org.wickedsource.docxstamper.walk.coordinates.ParagraphCoordinates;

import java.util.List;

public class PlaceholderReplacer<T> {

    private Logger logger = LoggerFactory.getLogger(PlaceholderReplacer.class);

    private ExpressionUtil expressionUtil = new ExpressionUtil();

    private ExpressionResolver expressionResolver = new ExpressionResolver();

    private TypeResolverRegistry typeResolverRegistry;

    private String lineBreakPlaceholder;

    public PlaceholderReplacer(TypeResolverRegistry typeResolverRegistry) {
        this.typeResolverRegistry = typeResolverRegistry;
    }

    public PlaceholderReplacer(TypeResolverRegistry typeResolverRegistry, String lineBreakPlaceholder) {
        this.typeResolverRegistry = typeResolverRegistry;
        this.lineBreakPlaceholder = lineBreakPlaceholder;
    }

    /**
     * Finds expressions in a document and resolves them against the specified context object. The expressions in the
     * document are then replaced by the resolved values.
     *
     * @param document          the document in which to replace all expressions.
     * @param expressionContext the context to resolve the expressions against.
     */
    public void resolveExpressions(final WordprocessingMLPackage document, final T expressionContext) {
        CoordinatesWalker walker = new BaseCoordinatesWalker(document) {
            @Override
            protected void onParagraph(ParagraphCoordinates paragraphCoordinates) {
                resolveExpressionsForParagraph(paragraphCoordinates.getParagraph(), expressionContext, document);
            }
        };
        walker.walk();
    }

    @SuppressWarnings("unchecked")
    public void resolveExpressionsForParagraph(P p, T expressionContext, WordprocessingMLPackage document) {
        ParagraphWrapper paragraphWrapper = new ParagraphWrapper(p);
        List<String> placeholders = expressionUtil.findVariableExpressions(paragraphWrapper.getText());
        for (String placeholder : placeholders) {
            try {
                Object replacement = expressionResolver.resolveExpression(placeholder, expressionContext);
                if (replacement != null) {
                    ITypeResolver resolver = typeResolverRegistry.getResolverForType(replacement.getClass());
                    Object replacementObject = resolver.resolve(document, replacement);
                    replace(paragraphWrapper, placeholder, replacementObject);
                    logger.debug(String.format("Replaced expression '%s' with value provided by TypeResolver %s", placeholder, resolver.getClass()));
                }
            } catch (SpelEvaluationException | SpelParseException e) {
                logger.warn(String.format(
                        "Expression %s could not be resolved against context root of type %s. Reason: %s. Set log level to TRACE to view Stacktrace.",
                        placeholder, expressionContext.getClass(), e.getMessage()));
                logger.trace("Reason for skipping expression:", e);
            }
        }
        if (this.lineBreakPlaceholder != null) {
            replaceLineBreaks(paragraphWrapper);
        }
    }

    private void replaceLineBreaks(ParagraphWrapper paragraphWrapper) {
        Br lineBreak = Context.getWmlObjectFactory().createBr();
        R run = RunUtil.create(lineBreak);
        while (paragraphWrapper.getText().contains(this.lineBreakPlaceholder)) {
            replace(paragraphWrapper, this.lineBreakPlaceholder, run);
        }
    }

    public void replace(ParagraphWrapper p, String placeholder, Object replacementObject) {
        if (replacementObject == null) {
            replacementObject = RunUtil.create("");
        }
        if (replacementObject instanceof R) {
            RunUtil.applyParagraphStyle(p.getParagraph(), (R) replacementObject);
        }
        p.replace(placeholder, replacementObject);
    }

}
