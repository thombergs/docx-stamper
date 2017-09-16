package org.wickedsource.docxstamper.el;

import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.wickedsource.docxstamper.api.EvaluationContextExtender;

/**
 * {@link org.wickedsource.docxstamper.api.EvaluationContextExtender} that does no customization.
 */
class EmptyEvaluationContextExtender implements EvaluationContextExtender {
    @Override
    public void configureEvaluationContext(StandardEvaluationContext context) {
        // don't customize it
    }
}
