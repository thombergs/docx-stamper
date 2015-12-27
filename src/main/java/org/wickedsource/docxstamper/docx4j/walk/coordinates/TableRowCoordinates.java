package org.wickedsource.docxstamper.docx4j.walk.coordinates;

import org.docx4j.wml.Tr;

public class TableRowCoordinates extends AbstractCoordinates {

    private final Tr row;

    private final int index;

    private final TableCoordinates parentTableCoordinates;


    public TableRowCoordinates(Tr row, int index, TableCoordinates parentTableCoordinates) {
        this.row = row;
        this.index = index;
        this.parentTableCoordinates = parentTableCoordinates;
    }

    public int getIndex() {
        return index;
    }

    public String toString() {
        String toString = String.format("paragraph at index %d", index);
        return parentTableCoordinates.toString() + "\n" + toString;
    }

    public Tr getRow() {
        return row;
    }

    public TableCoordinates getParentTableCoordinates() {
        return parentTableCoordinates;
    }

}
