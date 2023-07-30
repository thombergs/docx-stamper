package io.reflectoring.docxstamper.api.coordinates;

import org.docx4j.wml.Tc;

public class TableCellCoordinates extends AbstractCoordinates {

    private final Tc cell;

    private final int index;

    private final TableRowCoordinates parentTableRowCoordinates;


    public TableCellCoordinates(Tc cell, int index, TableRowCoordinates parentTableRowCoordinates) {
        this.cell = cell;
        this.index = index;
        this.parentTableRowCoordinates = parentTableRowCoordinates;
    }

    public int getIndex() {
        return index;
    }

    public String toString() {
        String toString = String.format("paragraph at index %d", index);
        return parentTableRowCoordinates.toString() + "\n" + toString;
    }

    public Tc getCell() {
        return cell;
    }

    public TableRowCoordinates getParentTableRowCoordinates() {
        return parentTableRowCoordinates;
    }
}
