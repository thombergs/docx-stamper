package org.wickedsource.docxstamper.processor.repeat;

import java.util.List;

/**
 * Interface for processors that can repeat a table row.
 *
 * @author joseph
 * @version $Id: $Id
 */
public interface IRepeatProcessor {

    /**
     * May be called to mark a table row to be copied once for each element in the passed-in list.
     * Within each copy of the row, all expressions are evaluated against one of the objects in the list.
     *
     * @param objects the objects which serve as context root for expressions found in the template table row.
     */
    void repeatTableRow(List<Object> objects);

}
