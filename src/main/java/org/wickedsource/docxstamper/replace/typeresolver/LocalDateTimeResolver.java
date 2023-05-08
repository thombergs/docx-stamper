package org.wickedsource.docxstamper.replace.typeresolver;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateTimeResolver extends AbstractToTextResolver<LocalDateTime> {
	private final DateTimeFormatter formatter;

	public LocalDateTimeResolver() {
		this(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
	}

	public LocalDateTimeResolver(DateTimeFormatter formatter) {
		this.formatter = formatter;
	}

	@Override
	protected String resolveStringForObject(LocalDateTime localDateTime) {
		return localDateTime.format(formatter);
	}
}
