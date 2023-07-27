package org.wickedsource.docxstamper.processor.repeat;

import java.util.List;

/**
 * Implementations of this interface are responsible for processing the repeat paragraph instruction.
 * The repeat paragraph instruction is a comment that contains the following text:
 * <p>
 * <code>
 * repeatParagraph(...)
 * </code>
 * <p>
 * Where the three dots represent an expression that evaluates to a list of objects.
 * The processor then copies the paragraph once for each object in the list and evaluates all expressions
 * within each copy against the respective object.
 *
 * @author joseph
 * @version $Id: $Id
 */
public interface IParagraphRepeatProcessor {

    /**
     * May be called to mark a paragraph to be copied once for each element in the passed-in list.
     * Within each copy of the row, all expressions are evaluated against one of the objects in the list.
     *
     * @param objects the objects which serve as context root for expressions found in the template table row.
     */
    void repeatParagraph(List<Object> objects);
}
