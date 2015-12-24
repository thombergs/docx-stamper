package org.wickedsource.docxstamper.walk;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.wickedsource.docxstamper.walk.coordinates.TableCellCoordinates;
import org.wickedsource.docxstamper.walk.coordinates.TableCoordinates;
import org.wickedsource.docxstamper.walk.coordinates.TableRowCoordinates;

public abstract class ParagraphWalker extends DocumentWalker {

    public ParagraphWalker(XWPFDocument document) {
        super(document);
    }

    @Override
    protected void onTable(TableCoordinates tableCoordinates) {
        // do nothing
    }

    @Override
    protected void onTableRow(TableRowCoordinates tableRowCoordinates) {
        // do nothing
    }

    @Override
    protected void onTableCell(TableCellCoordinates tableCellCoordinates) {
        // do nothing
    }
}
