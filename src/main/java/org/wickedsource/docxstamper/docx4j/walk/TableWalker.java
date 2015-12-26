package org.wickedsource.docxstamper.docx4j.walk;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.wickedsource.docxstamper.docx4j.walk.coordinates.ParagraphCoordinates;
import org.wickedsource.docxstamper.docx4j.walk.coordinates.TableCellCoordinates;
import org.wickedsource.docxstamper.docx4j.walk.coordinates.TableRowCoordinates;

public abstract class TableWalker extends DocumentWalker {

    public TableWalker(WordprocessingMLPackage document) {
        super(document);
    }

    @Override
    protected void onParagraph(ParagraphCoordinates paragraphCoordinates) {

    }

    @Override
    protected void onTableCell(TableCellCoordinates tableCellCoordinates) {

    }

    @Override
    protected void onTableRow(TableRowCoordinates tableRowCoordinates) {

    }
}
