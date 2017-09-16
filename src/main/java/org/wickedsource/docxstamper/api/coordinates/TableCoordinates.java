package org.wickedsource.docxstamper.api.coordinates;

import org.docx4j.wml.Tbl;

public class TableCoordinates extends AbstractCoordinates {

    private final Tbl table;

    private final int index;

    private final TableCellCoordinates parentTableCellCoordinates;

    public TableCoordinates(Tbl table, int index) {
        this.table = table;
        this.index = index;
        this.parentTableCellCoordinates = null;
    }

    public TableCoordinates(Tbl table, int index, TableCellCoordinates parentTableCellCoordinates) {
        this.table = table;
        this.index = index;
        this.parentTableCellCoordinates = parentTableCellCoordinates;
    }

    public int getIndex() {
        return index;
    }

    public String toString() {
        String toString = String.format("paragraph at index %d", index);
        if (parentTableCellCoordinates != null) {
            toString = parentTableCellCoordinates.toString() + "\n" + toString;
        }
        return toString;
    }

    public Tbl getTable() {
        return table;
    }

    public TableCellCoordinates getParentTableCellCoordinates() {
        return parentTableCellCoordinates;
    }
}
