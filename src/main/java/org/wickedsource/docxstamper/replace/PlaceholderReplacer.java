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
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.wickedsource.docxstamper.api.UnresolvedExpressionException;
import org.wickedsource.docxstamper.api.typeresolver.ITypeResolver;
import org.wickedsource.docxstamper.api.typeresolver.TypeResolverRegistry;
import org.wickedsource.docxstamper.el.ExpressionResolver;
import org.wickedsource.docxstamper.el.ExpressionUtil;
import org.wickedsource.docxstamper.util.ParagraphWrapper;
import org.wickedsource.docxstamper.util.RunUtil;
import org.wickedsource.docxstamper.util.walk.BaseCoordinatesWalker;
import org.wickedsource.docxstamper.util.walk.CoordinatesWalker;

import java.util.List;
import java.util.Optional;

public class PlaceholderReplacer {
	private final Logger logger = LoggerFactory.getLogger(PlaceholderReplacer.class);
	private final ExpressionResolver expressionResolver;
	private final TypeResolverRegistry typeResolverRegistry;
	private final boolean replaceNullValues;
	private final String nullValuesDefault;
	private final boolean failOnUnresolvedExpression;
	private final boolean leaveEmptyOnExpressionError;
	private final boolean replaceUnresolvedExpressions;
	private final String unresolvedExpressionsDefaultValue;
	private final String lineBreakPlaceholder;

	public PlaceholderReplacer(
			TypeResolverRegistry typeResolverRegistry,
			StandardEvaluationContext standardEvaluationContext,
			boolean replaceNullValues,
			String nullValuesDefault,
			boolean failOnUnresolvedExpression1,
			boolean replaceUnresolvedExpressions1,
			String unresolvedExpressionsDefaultValue1,
			boolean leaveEmptyOnExpressionError1,
			String lineBreakPlaceholder1
	) {
		this.typeResolverRegistry = typeResolverRegistry;
		this.expressionResolver = new ExpressionResolver(standardEvaluationContext);
		this.replaceNullValues = replaceNullValues;
		this.nullValuesDefault = nullValuesDefault;
		this.failOnUnresolvedExpression = failOnUnresolvedExpression1;
		this.replaceUnresolvedExpressions = replaceUnresolvedExpressions1;
		this.unresolvedExpressionsDefaultValue = unresolvedExpressionsDefaultValue1;
		this.leaveEmptyOnExpressionError = leaveEmptyOnExpressionError1;
		this.lineBreakPlaceholder = lineBreakPlaceholder1;
	}

	/**
	 * Finds expressions in a document and resolves them against the specified context object. The expressions in the
	 * document are then replaced by the resolved values.
	 *
	 * @param document          the document in which to replace all expressions.
	 * @param expressionContext the context root
	 */
	public void resolveExpressions(final WordprocessingMLPackage document, Object expressionContext) {
		CoordinatesWalker walker = new BaseCoordinatesWalker(document) {
			@Override
			protected void onParagraph(P paragraph) {
				resolveExpressionsForParagraph(paragraph, expressionContext, document);
			}
		};
		walker.walk();
	}

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
					logger.debug(String.format("Replaced expression '%s' with value provided by TypeResolver %s",
											   placeholder,
											   resolver.getClass()));
				} else if (replaceNullValues) {
					//noinspection rawtypes
					ITypeResolver resolver = typeResolverRegistry.getDefaultResolver();
					R replacementObject = resolver.resolve(document, nullValuesDefault);
					replace(paragraphWrapper, placeholder, replacementObject);
					logger.debug(String.format("Replaced expression '%s' with value provided by TypeResolver %s",
											   placeholder,
											   resolver.getClass()));
				}
			} catch (SpelEvaluationException | SpelParseException e) {
				if (isFailOnUnresolvedExpression()) {
					throw new UnresolvedExpressionException(
							String.format(
									"Expression %s could not be resolved against context root of type %s. Reason: %s. Set log level to TRACE to view Stacktrace.",
									placeholder,
									expressionContext.getClass(),
									e.getMessage()),
							e);
				} else {
					logger.warn(String.format(
							"Expression %s could not be resolved against context root of type %s. Reason: %s. Set log level to TRACE to view Stacktrace.",
							placeholder,
							expressionContext.getClass(),
							e.getMessage()));
					logger.trace("Reason for skipping expression:", e);

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
