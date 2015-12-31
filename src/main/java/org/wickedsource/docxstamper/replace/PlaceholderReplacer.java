package org.wickedsource.docxstamper.replace;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.spel.SpelEvaluationException;
import org.wickedsource.docxstamper.el.ExpressionResolver;
import org.wickedsource.docxstamper.el.ExpressionUtil;
import org.wickedsource.docxstamper.walk.coordinates.BaseCoordinatesWalker;
import org.wickedsource.docxstamper.walk.coordinates.CoordinatesWalker;
import org.wickedsource.docxstamper.walk.coordinates.ParagraphCoordinates;

import java.util.List;

public class PlaceholderReplacer<T> {

    private Logger logger = LoggerFactory.getLogger(PlaceholderReplacer.class);

    private ExpressionUtil expressionUtil = new ExpressionUtil();

    private ExpressionResolver expressionResolver = new ExpressionResolver();

    private TypeResolverRegistry typeResolverRegistry;

    public PlaceholderReplacer(TypeResolverRegistry typeResolverRegistry) {
        this.typeResolverRegistry = typeResolverRegistry;
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
        ParagraphWrapper aggregator = new ParagraphWrapper(p);
        List<String> placeholders = expressionUtil.findExpressions(aggregator.getText());
        for (String placeholder : placeholders) {
            try {
                Object replacement = expressionResolver.resolveExpression(placeholder, expressionContext);
                if (replacement != null) {
                    int replacementIndex = aggregator.cleanPlaceholder(placeholder);
                    TypeResolver resolver = typeResolverRegistry.getResolverForType(replacement.getClass());
                    Object replacementObject = resolver.resolve(document, replacement);
                    p.getContent().add(replacementIndex, replacementObject);
                    aggregator.recalculateRuns();
                    logger.debug(String.format("Replaced expression '%s' with value provided by TypeResolver %s", placeholder, resolver.getClass()));
                }
            } catch (SpelEvaluationException e) {
                logger.warn(String.format("Expression %s could not be resolved against context root of type %s", placeholder, expressionContext.getClass()));
            }
        }
    }

}
