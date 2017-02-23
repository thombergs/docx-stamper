package org.wickedsource.docxstamper.replace.typeresolver;

/**
 * This ITypeResolver may serve as a fallback when there is no ITypeResolver available for a certain type. Hence, this
 * resolver is able to map all objects to their String value.
 */
public class FallbackResolver extends AbstractToTextResolver<Object> {

    @Override
    protected String resolveStringForObject(Object object) {
        return String.valueOf(object);
    }

}
