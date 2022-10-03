package org.wickedsource.docxstamper.util.walk;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.wickedsource.docxstamper.api.coordinates.RunCoordinates;
import org.wickedsource.docxstamper.api.coordinates.TableCellCoordinates;
import org.wickedsource.docxstamper.api.coordinates.TableCoordinates;
import org.wickedsource.docxstamper.api.coordinates.TableRowCoordinates;
import org.wickedsource.docxstamper.util.DocumentUtil;

import java.util.ArrayList;
import java.util.List;

public abstract class CoordinatesWalker {

    private final WordprocessingMLPackage document;

    public CoordinatesWalker(WordprocessingMLPackage document) {
        this.document = document;
    }

    public void walk() {

        List<P> paragraphs = DocumentUtil.extractElements(document, P.class);
        for (P paragraph: paragraphs) {
            walkParagraph(paragraph);
        }
    }

    private void walkParagraph(P paragraph){
        int rowIndex = 0;

        // Creating a copy of the content helps avoid a concurrent modification exception
        List<Object> content = new ArrayList<>(paragraph.getContent());

        for (Object contentElement : content) {
            if (XmlUtils.unwrap(contentElement) instanceof R) {
                R run = (R) contentElement;
                RunCoordinates runCoordinates = new RunCoordinates(run, rowIndex);
                onRun(runCoordinates, paragraph);
            }
        }

        // we run the paragraph afterwards so that the comments inside work before the whole paragraph comments
        onParagraph(paragraph);

    }

    protected abstract void onParagraph(P paragraph);

    protected abstract void onRun(RunCoordinates runCoordinates, P paragraph);

    protected abstract void onTable(TableCoordinates tableCoordinates);

    protected abstract void onTableCell(TableCellCoordinates tableCellCoordinates);

    protected abstract void onTableRow(TableRowCoordinates tableRowCoordinates);

}
