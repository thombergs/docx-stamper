package org.wickedsource.docxstamper.walk.coordinates;

import org.docx4j.wml.P;
import org.wickedsource.docxstamper.api.coordinates.AbstractCoordinates;
import org.wickedsource.docxstamper.api.coordinates.TableCellCoordinates;

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
            toString = parentTableCellCoordinates.toString() + "\n" + toString;
        }
        return toString;
    }
}
