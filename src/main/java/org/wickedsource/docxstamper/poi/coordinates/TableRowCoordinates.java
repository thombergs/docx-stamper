package org.wickedsource.docxstamper.poi.coordinates;

import org.apache.poi.xwpf.usermodel.XWPFTableRow;

public class TableRowCoordinates {

    private final XWPFTableRow row;

    private final int index;

    private final TableCoordinates parentTableCoordinates;

    public TableRowCoordinates(XWPFTableRow row, int index, TableCoordinates parentTableCoordinates) {
        this.row = row;
        this.index = index;
        this.parentTableCoordinates = parentTableCoordinates;
    }

    public XWPFTableRow getRow() {
        return row;
    }

    public int getIndex() {
        return index;
    }

    public TableCoordinates getParentTableCoordinates() {
        return parentTableCoordinates;
    }

    public String toString() {
        String toString = String.format("table row at index %d", index);
        if (parentTableCoordinates != null) {
            return parentTableCoordinates + ",\n" + toString;
        } else {
            return toString;
        }
    }
}
