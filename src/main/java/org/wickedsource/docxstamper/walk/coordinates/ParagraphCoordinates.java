package org.wickedsource.docxstamper.walk.coordinates;

import org.apache.poi.xwpf.usermodel.XWPFParagraph;

public class ParagraphCoordinates {

    private final XWPFParagraph paragraph;

    private final int index;

    private final TableCellCoordinates parentTableCellCoordinates;

    public ParagraphCoordinates(XWPFParagraph paragraph, int index, TableCellCoordinates parentTableCellCoordinates) {
        this.paragraph = paragraph;
        this.index = index;
        this.parentTableCellCoordinates = parentTableCellCoordinates;
    }

    public XWPFParagraph getParagraph() {
        return paragraph;
    }

    public int getIndex() {
        return index;
    }

    public TableCellCoordinates getParentTableCellCoordinates() {
        return parentTableCellCoordinates;
    }

    public String toString(){
        String toString = String.format("paragraph at index %d", index);
        if(parentTableCellCoordinates != null){
            return parentTableCellCoordinates +  ",\n" + toString;
        }else{
            return toString;
        }
    }
}
