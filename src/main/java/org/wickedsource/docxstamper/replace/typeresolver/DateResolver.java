package org.wickedsource.docxstamper.replace.typeresolver;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * This ITypeResolver creates a formatted date String for expressions that return a Date object.
 */
public class DateResolver extends AbstractToTextResolver<Date> {

	private final DateTimeFormatter formatter;

	public DateResolver() {
		this(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
	}

	/**
	 * Creates a new DateResolver.
	 *
	 * @param formatter the format to use for date formatting. See java.text.SimpleDateFormat.
	 */
	public DateResolver(DateTimeFormatter formatter) {
		this.formatter = formatter;
	}

	@Override
	protected String resolveStringForObject(Date date) {
		return formatter.format(date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
	}
}
