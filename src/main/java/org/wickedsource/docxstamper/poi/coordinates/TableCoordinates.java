package org.wickedsource.docxstamper.poi.coordinates;

import org.apache.poi.xwpf.usermodel.XWPFTable;

public class TableCoordinates {

    private final XWPFTable table;

    private final int index;

    private final TableCellCoordinates parentTableCellCoordinates;

    public TableCoordinates(XWPFTable table, int index) {
        this.table = table;
        this.index = index;
        this.parentTableCellCoordinates = null;
    }

    public TableCoordinates(XWPFTable table, int index, TableCellCoordinates parentTableCellCoordinates) {
        this.table = table;
        this.index = index;
        this.parentTableCellCoordinates = parentTableCellCoordinates;
    }

    public XWPFTable getTable() {
        return table;
    }

    public int getIndex() {
        return index;
    }

    public TableCellCoordinates getParentTableCellCoordinates() {
        return parentTableCellCoordinates;
    }

    public String toString() {
        String toString = String.format("table at index %d", index);
        if (parentTableCellCoordinates != null) {
            return parentTableCellCoordinates + ",\n" + toString;
        } else {
            return toString;
        }
    }
}
