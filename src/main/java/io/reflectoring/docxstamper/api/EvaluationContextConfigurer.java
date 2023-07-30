package io.reflectoring.docxstamper.api;

import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * Allows for custom configuration of a spring expression language {@link org.springframework.expression.EvaluationContext}.
 * This can  for example be used to add custom {@link org.springframework.expression.PropertyAccessor}s and {@link org.springframework.expression.MethodResolver}s.
 */
public interface EvaluationContextConfigurer {

    /**
     * Configure the context before it's used by docxstamper.
     *
     * @param context the spel eval context, not null
     */
    void configureEvaluationContext(StandardEvaluationContext context);

}
