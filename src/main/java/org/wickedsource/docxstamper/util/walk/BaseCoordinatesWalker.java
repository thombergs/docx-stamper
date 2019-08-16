package org.wickedsource.docxstamper.util.walk;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.wickedsource.docxstamper.api.coordinates.*;

public abstract class BaseCoordinatesWalker extends CoordinatesWalker {

    public BaseCoordinatesWalker(WordprocessingMLPackage document) {
        super(document);
    }

    @Override
    protected void onParagraph(ParagraphCoordinates paragraphCoordinates) {

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
    
    @Override
	protected void onRun(RunCoordinates runCoordinates, ParagraphCoordinates paragraphCoordinates) {

	}
}
