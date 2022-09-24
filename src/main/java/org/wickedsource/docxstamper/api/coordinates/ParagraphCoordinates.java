package org.wickedsource.docxstamper.api.coordinates;

import org.docx4j.wml.P;

public class ParagraphCoordinates extends AbstractCoordinates {

    private final P paragraph;

    private final int index;

    private final TableCellCoordinates parentTableCellCoordinates;


    public ParagraphCoordinates(P paragraph, int index, TableCellCoordinates parentTableCellCoordinates) {
        this.paragraph = paragraph;
        this.index = index;
        this.parentTableCellCoordinates = parentTableCellCoordinates;
    }

    public ParagraphCoordinates(P paragraph, int index) {
        this.paragraph = paragraph;
        this.index = index;
        this.parentTableCellCoordinates = null;
    }

    public P getParagraph() {
        return paragraph;
    }

    public int getIndex() {
        return index;
    }

    public TableCellCoordinates getParentTableCellCoordinates() {
        return parentTableCellCoordinates;
    }

    public String toString() {
        String toString = String.format("paragraph at index %d", index);
        if (parentTableCellCoordinates != null) {
            toString = parentTableCellCoordinates + "\n" + toString;
        }
        return toString;
    }
}
