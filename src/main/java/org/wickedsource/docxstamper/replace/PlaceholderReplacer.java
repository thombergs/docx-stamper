package org.wickedsource.docxstamper.replace;

import java.util.List;

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
import org.wickedsource.docxstamper.api.coordinates.ParagraphCoordinates;
import org.wickedsource.docxstamper.api.typeresolver.ITypeResolver;
import org.wickedsource.docxstamper.api.typeresolver.TypeResolverRegistry;
import org.wickedsource.docxstamper.el.ExpressionResolver;
import org.wickedsource.docxstamper.el.ExpressionUtil;
import org.wickedsource.docxstamper.proxy.ProxyBuilder;
import org.wickedsource.docxstamper.proxy.ProxyException;
import org.wickedsource.docxstamper.util.ParagraphWrapper;
import org.wickedsource.docxstamper.util.RunUtil;
import org.wickedsource.docxstamper.util.walk.BaseCoordinatesWalker;
import org.wickedsource.docxstamper.util.walk.CoordinatesWalker;

public class PlaceholderReplacer<T> {

  private Logger logger = LoggerFactory.getLogger(PlaceholderReplacer.class);

  private ExpressionUtil expressionUtil = new ExpressionUtil();

  private ExpressionResolver expressionResolver = new ExpressionResolver();

  private TypeResolverRegistry typeResolverRegistry;

  private String lineBreakPlaceholder;

  private boolean leaveEmptyOnExpressionError = false;

  public PlaceholderReplacer(TypeResolverRegistry typeResolverRegistry) {
    this.typeResolverRegistry = typeResolverRegistry;
  }

  public PlaceholderReplacer(TypeResolverRegistry typeResolverRegistry, String lineBreakPlaceholder) {
    this.typeResolverRegistry = typeResolverRegistry;
    this.lineBreakPlaceholder = lineBreakPlaceholder;
  }

  public boolean isLeaveEmptyOnExpressionError() {
    return leaveEmptyOnExpressionError;
  }

  public void setLeaveEmptyOnExpressionError(boolean leaveEmptyOnExpressionError) {
    this.leaveEmptyOnExpressionError = leaveEmptyOnExpressionError;
  }

  public void setExpressionResolver(ExpressionResolver expressionResolver) {
    this.expressionResolver = expressionResolver;
  }

  /**
   * Finds expressions in a document and resolves them against the specified context object. The expressions in the
   * document are then replaced by the resolved values.
   *
   * @param document          the document in which to replace all expressions.
   * @param proxyBuilder      builder for a proxy around the context root to customize its interface
   */
  public void resolveExpressions(final WordprocessingMLPackage document, ProxyBuilder<T> proxyBuilder) {
    try {
      final T expressionContext = proxyBuilder.build();
      CoordinatesWalker walker = new BaseCoordinatesWalker(document) {
        @Override
        protected void onParagraph(ParagraphCoordinates paragraphCoordinates) {
          resolveExpressionsForParagraph(paragraphCoordinates.getParagraph(), expressionContext, document);
        }
      };
      walker.walk();
    } catch (ProxyException e) {
      throw new DocxStamperException("could not create proxy around context root!", e);
    }
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

        if(isLeaveEmptyOnExpressionError()) {
          replace(paragraphWrapper,placeholder,null);
        }
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
