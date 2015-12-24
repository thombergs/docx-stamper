package org.wickedsource.docxstamper;

import java.util.List;

public interface TemplateMapping<T> {

    /**
     * Liefert eine Liste aller Platzhalter, die im Word-Template ersetzt werden sollen.
     */
    public List<String> getPlaceholders();

    /**
     * Liefert f�r einen Platzhalter den Wert aus den Daten, durch den der Platzhalter im Word-Template ersetzt werden soll.
     */
    public String getReplacementForPlaceholder(String placeholder, T data);

}
