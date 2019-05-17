package org.wickedsource.docxstamper.processor.repeat;

import java.util.List;

public interface IRepeatTable {

    /**
     * May be called to mark a table row to be copied once for each element in the passed-in list.
     * Within each copy of the row, all expressions are evaluated against one of the objects in the list.
     *
     * @param objects the objects which serve as context root for expressions found in the template table row.
     */
    void repeatTable(List<Object> objects);

}
