package org.wickedsource.docxstamper.processor.displayif;

/**
 * Interface for processors that may be used to delete commented paragraphs or tables from the document, depending on a given condition.
 *
 * @author joseph
 * @version $Id: $Id
 */
public interface IDisplayIfProcessor {

    /**
     * May be called to delete the commented paragraph or not, depending on the given boolean condition.
     *
     * @param condition if true, the commented paragraph will remain in the document. If false, the commented paragraph
     *                  will be deleted at stamping.
     */
    void displayParagraphIf(Boolean condition);

    /**
     * May be called to delete the commented paragraph or not, depending on the presence of the given data.
     *
     * @param condition if non null, the commented paragraph will remain in the document. If null, the commented paragraph
     *                  will be deleted at stamping.
     */
    void displayParagraphIfPresent(Object condition);

    /**
     * May be called to delete the table surrounding the commented paragraph, depending on the given boolean condition.
     *
     * @param condition if true, the table row surrounding the commented paragraph will remain in the document. If false, the table row
     *                  will be deleted at stamping.
     */
    void displayTableRowIf(Boolean condition);

    /**
     * May be called to delete the table surrounding the commented paragraph, depending on the given boolean condition.
     *
     * @param condition if true, the table surrounding the commented paragraph will remain in the document. If false, the table
     *                  will be deleted at stamping.
     */
    void displayTableIf(Boolean condition);

}
