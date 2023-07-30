package io.reflectoring.docxstamper.util.walk;

import io.reflectoring.docxstamper.api.coordinates.ParagraphCoordinates;
import io.reflectoring.docxstamper.api.coordinates.RunCoordinates;
import io.reflectoring.docxstamper.api.coordinates.TableCellCoordinates;
import io.reflectoring.docxstamper.api.coordinates.TableCoordinates;
import io.reflectoring.docxstamper.api.coordinates.TableRowCoordinates;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import io.reflectoring.docxstamper.api.coordinates.*;

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
