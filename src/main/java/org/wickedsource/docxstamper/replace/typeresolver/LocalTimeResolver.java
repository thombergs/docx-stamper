package org.wickedsource.docxstamper.replace.typeresolver;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class LocalTimeResolver extends AbstractToTextResolver<LocalTime> {
	private final DateTimeFormatter formatter;

	public LocalTimeResolver() {
		this(DateTimeFormatter.ISO_LOCAL_TIME);
	}

	public LocalTimeResolver(DateTimeFormatter formatter) {
		this.formatter = formatter;
	}

	@Override
	protected String resolveStringForObject(LocalTime localTime) {
		return localTime.format(formatter);
	}
}
