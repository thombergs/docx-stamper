package org.wickedsource.docxstamper.replace.typeresolver;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LocalDateResolver extends AbstractToTextResolver<LocalDate> {
	private final DateTimeFormatter formatter;

	public LocalDateResolver() {
		this(DateTimeFormatter.ISO_LOCAL_DATE);
	}

	public LocalDateResolver(DateTimeFormatter formatter) {
		this.formatter = formatter;
	}

	@Override
	protected String resolveStringForObject(LocalDate localDateTime) {
		return localDateTime.format(formatter);
	}
}
