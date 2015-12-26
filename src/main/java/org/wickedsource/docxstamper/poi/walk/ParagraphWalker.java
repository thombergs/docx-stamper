package org.wickedsource.docxstamper.poi.walk;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.wickedsource.docxstamper.poi.coordinates.TableCellCoordinates;
import org.wickedsource.docxstamper.poi.coordinates.TableCoordinates;
import org.wickedsource.docxstamper.poi.coordinates.TableRowCoordinates;

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
