package io.reflectoring.docxstamper.el;

import io.reflectoring.docxstamper.api.EvaluationContextConfigurer;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * {@link EvaluationContextConfigurer} that does no customization.
 */
public class NoOpEvaluationContextConfigurer implements EvaluationContextConfigurer {

    @Override
    public void configureEvaluationContext(StandardEvaluationContext context) {
        // don't customize it
    }

}
