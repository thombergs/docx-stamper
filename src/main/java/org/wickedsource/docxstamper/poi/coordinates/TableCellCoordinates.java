package org.wickedsource.docxstamper.poi.coordinates;

import org.apache.poi.xwpf.usermodel.XWPFTableCell;

public class TableCellCoordinates {

    private final XWPFTableCell cell;

    private final int index;

    private final TableRowCoordinates parentTableRowCoordinates;

    public TableCellCoordinates(XWPFTableCell cell, int index, TableRowCoordinates parentTableRowCoordinates) {
        this.cell = cell;
        this.index = index;
        this.parentTableRowCoordinates = parentTableRowCoordinates;
    }

    public XWPFTableCell getCell() {
        return cell;
    }

    public int getIndex() {
        return index;
    }

    public TableRowCoordinates getParentTableRowCoordinates() {
        return parentTableRowCoordinates;
    }

    public String toString() {
        String toString = String.format("table cell at index %d", index);
        if (parentTableRowCoordinates != null) {
            return parentTableRowCoordinates + ",\n" + toString;
        } else {
            return toString;
        }
    }
}
