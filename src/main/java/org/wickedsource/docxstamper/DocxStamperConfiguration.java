package org.wickedsource.docxstamper;

/**
 * Provides configuration parameters for DocxStamper.
 */
public class DocxStamperConfiguration {

    private String lineBreakPlaceholder;

    public String getLineBreakPlaceholder() {
        return lineBreakPlaceholder;
    }

    /**
     * The String provided as lineBreakPlaceholder will be replaces with a line break
     * when stamping a document. If no lineBreakPlaceholder is provided, no replacement
     * will take place.
     *
     * @param lineBreakPlaceholder the String that should be replaced with line breaks during stamping.
     * @return the configuration object for chaining.
     */
    public DocxStamperConfiguration setLineBreakPlaceholder(String lineBreakPlaceholder) {
        this.lineBreakPlaceholder = lineBreakPlaceholder;
        return this;
    }
}
