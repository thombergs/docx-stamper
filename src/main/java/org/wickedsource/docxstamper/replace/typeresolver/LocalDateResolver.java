package org.wickedsource.docxstamper.replace.typeresolver;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Resolves {@link java.time.LocalDate} objects by formatting them with a {@link java.time.format.DateTimeFormatter}.
 *
 * @author joseph
 * @version $Id: $Id
 */
public class LocalDateResolver extends AbstractToTextResolver<LocalDate> {
	private final DateTimeFormatter formatter;

    /**
     * Uses {@link java.time.format.DateTimeFormatter#ISO_LOCAL_DATE} for formatting.
     */
	public LocalDateResolver() {
		this(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    /**
     * Uses the given formatter for formatting.
     *
     * @param formatter the formatter to use.
     */
	public LocalDateResolver(DateTimeFormatter formatter) {
		this.formatter = formatter;
    }

    /**
     * {@inheritDoc}
     */
	@Override
	protected String resolveStringForObject(LocalDate localDateTime) {
		return localDateTime.format(formatter);
	}
}
