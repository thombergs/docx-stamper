package org.wickedsource.docxstamper.walk.coordinates;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.wickedsource.docxstamper.util.CommentWrapper;

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
	protected CommentWrapper onRun(RunCoordinates runCoordinates, ParagraphCoordinates paragraphCoordinates) {
		return null;
	}
}
