package org.wickedsource.docxstamper.docx4j.walk;

import org.docx4j.wml.ContentAccessor;
import org.wickedsource.docxstamper.docx4j.walk.coordinates.ParagraphCoordinates;
import org.wickedsource.docxstamper.docx4j.walk.coordinates.TableCellCoordinates;
import org.wickedsource.docxstamper.docx4j.walk.coordinates.TableCoordinates;

public abstract class TableRowWalker extends DocumentWalker {

    public TableRowWalker(ContentAccessor parentObject) {
        super(parentObject);
    }

    @Override
    protected void onTable(TableCoordinates tableCoordinates) {

    }

    @Override
    protected void onTableCell(TableCellCoordinates tableCellCoordinates) {

    }

    @Override
    protected void onParagraph(ParagraphCoordinates paragraphCoordinates) {

    }

}
