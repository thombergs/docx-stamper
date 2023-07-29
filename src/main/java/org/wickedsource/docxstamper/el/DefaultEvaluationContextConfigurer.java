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
 * {@link org.wickedsource.docxstamper.api.EvaluationContextConfigurer} that has a better default security,
 * especially doesn't allow Especially known injections.
 *
 * @author joseph
 * @version $Id: $Id
 */
public class DefaultEvaluationContextConfigurer implements EvaluationContextConfigurer {
    /**
     * {@inheritDoc}
     */
    @Override
    public void configureEvaluationContext(StandardEvaluationContext context) {
        TypeLocator typeLocator = typeName -> {
            throw new SpelEvaluationException(SpelMessage.TYPE_NOT_FOUND, typeName);
        };
        context.setPropertyAccessors(List.of(DataBindingPropertyAccessor.forReadWriteAccess()));
        context.setConstructorResolvers(Collections.emptyList());
        context.setMethodResolvers(new ArrayList<>(List.of(DataBindingMethodResolver.forInstanceMethodInvocation())));
        context.setBeanResolver(null);
        context.setTypeLocator(typeLocator);
        context.setTypeConverter(new StandardTypeConverter());
        context.setTypeComparator(new StandardTypeComparator());
        context.setOperatorOverloader(new StandardOperatorOverloader());
    }
}
