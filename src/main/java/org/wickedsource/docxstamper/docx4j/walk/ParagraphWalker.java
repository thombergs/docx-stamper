package org.wickedsource.docxstamper.docx4j.walk;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.wickedsource.docxstamper.docx4j.walk.coordinates.TableCellCoordinates;
import org.wickedsource.docxstamper.docx4j.walk.coordinates.TableCoordinates;
import org.wickedsource.docxstamper.docx4j.walk.coordinates.TableRowCoordinates;

public abstract class ParagraphWalker extends DocumentWalker {

    public ParagraphWalker(WordprocessingMLPackage document) {
        super(document);
    }

    @Override
    protected void onTable(TableCoordinates tableCoordinates) {

    }

    @Override
    protected void onTableCell(TableCellCoordinates tableCellCoordinates) {

    }

    @Override
    protected void onTableRow(TableRowCoordinates tableRowCoordinates) {

    }
}
