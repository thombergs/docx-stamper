package org.wickedsource.docxstamper.docx4j.walk;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.wickedsource.docxstamper.docx4j.walk.coordinates.ParagraphCoordinates;
import org.wickedsource.docxstamper.docx4j.walk.coordinates.TableCoordinates;
import org.wickedsource.docxstamper.docx4j.walk.coordinates.TableRowCoordinates;

public abstract class TableCellWalker extends DocumentWalker {

    public TableCellWalker(WordprocessingMLPackage document) {
        super(document);
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
