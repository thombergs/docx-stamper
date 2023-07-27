package org.wickedsource.docxstamper.replace.typeresolver;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Resolves {@link java.time.LocalDateTime} values to a formatted string.
 *
 * @author joseph
 * @version $Id: $Id
 */
public class LocalDateTimeResolver extends AbstractToTextResolver<LocalDateTime> {
	private final DateTimeFormatter formatter;

    /**
     * Creates a new resolver that uses {@link java.time.format.DateTimeFormatter#ISO_LOCAL_DATE_TIME} to format {@link java.time.LocalDateTime}
     * values.
     */
	public LocalDateTimeResolver() {
		this(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    /**
     * Creates a new resolver that uses the given formatter to format {@link java.time.LocalDateTime} values.
     *
     * @param formatter the formatter to use.
     */
	public LocalDateTimeResolver(DateTimeFormatter formatter) {
		this.formatter = formatter;
    }

    /**
     * {@inheritDoc}
     */
	@Override
	protected String resolveStringForObject(LocalDateTime localDateTime) {
		return localDateTime.format(formatter);
	}
}
