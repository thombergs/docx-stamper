package org.wickedsource.docxstamper.el;

import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.wickedsource.docxstamper.api.EvaluationContextConfigurer;

/**
 * {@link EvaluationContextConfigurer} that does no customization.
 */
public class NoOpEvaluationContextConfigurer implements EvaluationContextConfigurer {
	@Override
	public void configureEvaluationContext(StandardEvaluationContext context) {
		// don't customize it
	}
}
