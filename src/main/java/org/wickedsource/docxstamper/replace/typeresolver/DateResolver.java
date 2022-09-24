package org.wickedsource.docxstamper.replace.typeresolver;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This ITypeResolver creates a formatted date String for expressions that return a Date object.
 */
public class DateResolver extends AbstractToTextResolver<Date> {

    private final String formatString;

    private final DateFormat dateFormat;

    /**
     * Creates a new DateResolver.
     *
     * @param formatString the format to use for date formatting. See java.text.SimpleDateFormat.
     */
    public DateResolver(String formatString) {
        this.formatString = formatString;
        this.dateFormat = new SimpleDateFormat(formatString);
    }

    @Override
    protected String resolveStringForObject(Date date) {
        synchronized (dateFormat) {
            return dateFormat.format(date);
        }
    }

    public String getFormatString() {
        return formatString;
    }

}
