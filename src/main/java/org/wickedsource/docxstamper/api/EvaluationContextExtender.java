package org.wickedsource.docxstamper.api;

import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * Allows for custom configuration of an spring expression language {@link org.springframework.expression.EvaluationContext}.
 * This can be used to add custom {@link org.springframework.expression.PropertyAccessor}s and {@link org.springframework.expression.MethodResolver}s.
 */
public interface EvaluationContextExtender {

    /**
     * Configure the context before it's used by docxstamper.
     *
     * @param context the spel eval context, not null
     */
    void configureEvaluationContext(StandardEvaluationContext context);

}
