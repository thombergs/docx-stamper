package org.wickedsource.docxstamper.replace.typeresolver;

/**
 * This ITypeResolver may serve as a fallback when there is no ITypeResolver available for a certain type. Hence, this
 * resolver is able to map all objects to their String value.
 *
 * @author joseph
 * @version $Id: $Id
 */
public class FallbackResolver extends AbstractToTextResolver<Object> {
    /**
     * {@inheritDoc}
     */
	@Override
	protected String resolveStringForObject(Object object) {
		return object != null ? String.valueOf(object) : "";
	}
}
