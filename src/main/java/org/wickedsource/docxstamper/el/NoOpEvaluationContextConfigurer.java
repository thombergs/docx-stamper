package org.wickedsource.docxstamper.el;

import org.springframework.expression.TypeLocator;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.SpelMessage;
import org.springframework.expression.spel.support.*;
import org.wickedsource.docxstamper.api.EvaluationContextConfigurer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@link EvaluationContextConfigurer} that does no customization.
 */
public class NoOpEvaluationContextConfigurer implements EvaluationContextConfigurer {
    @Override
    public void configureEvaluationContext(StandardEvaluationContext context) {
        // DO NOTHING
    }
}
