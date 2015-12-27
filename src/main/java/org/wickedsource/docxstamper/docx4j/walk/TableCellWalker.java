package org.wickedsource.docxstamper.docx4j.walk;

import org.docx4j.wml.ContentAccessor;
import org.wickedsource.docxstamper.docx4j.walk.coordinates.ParagraphCoordinates;
import org.wickedsource.docxstamper.docx4j.walk.coordinates.TableCoordinates;
import org.wickedsource.docxstamper.docx4j.walk.coordinates.TableRowCoordinates;

public abstract class TableCellWalker extends DocumentWalker {

    public TableCellWalker(ContentAccessor parentObject) {
        super(parentObject);
    }

    @Override
    protected void onTable(TableCoordinates tableCoordinates) {

    }

    @Override
    protected void onParagraph(ParagraphCoordinates paragraphCoordinates) {

    }

    @Override
    protected void onTableRow(TableRowCoordinates tableRowCoordinates) {

    }
}
