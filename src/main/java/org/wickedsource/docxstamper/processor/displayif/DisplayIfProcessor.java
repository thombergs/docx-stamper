package org.wickedsource.docxstamper.processor.displayif;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Tr;
import org.wickedsource.docxstamper.processor.BaseCommentProcessor;
import org.wickedsource.docxstamper.processor.CommentProcessingException;
import org.wickedsource.docxstamper.util.ObjectDeleter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DisplayIfProcessor extends BaseCommentProcessor implements IDisplayIfProcessor {

    private List<P> paragraphsToBeRemoved = new ArrayList<>();

    private List<Tbl> tablesToBeRemoved = new ArrayList<>();

    private List<Tr> tableRowsToBeRemoved = new ArrayList<>();

    private List<Objects> objectsToBeRemoved = new ArrayList<>();

    public DisplayIfProcessor() {
    }

    @Override
    public void commitChanges(WordprocessingMLPackage document) {
        ObjectDeleter deleter = new ObjectDeleter();
        removeParagraphs(deleter);
        removeTables(deleter);
        removeTableRows(deleter);
        removeObjects(deleter);
    }

    @Override
    public void reset() {
        paragraphsToBeRemoved = new ArrayList<>();
        tablesToBeRemoved = new ArrayList<>();
        tableRowsToBeRemoved = new ArrayList<>();
        objectsToBeRemoved = new ArrayList<>();
    }

    private void removeParagraphs(ObjectDeleter deleter) {
        for (P paragraph : paragraphsToBeRemoved) {
            deleter.deleteParagraph(paragraph);
        }
    }

    private void removeObjects(ObjectDeleter deleter) {
        for (Object object : objectsToBeRemoved) {
            deleter.deleteObject(object);
        }
    }

    private void removeTables(ObjectDeleter deleter) {
        for (Tbl table : tablesToBeRemoved) {
            deleter.deleteTable(table);
        }
    }

    private void removeTableRows(ObjectDeleter deleter) {
        for (Tr tableRow : tableRowsToBeRemoved) {
            deleter.deleteTableRow(tableRow);
        }
    }

    @Override
    public void displayParagraphIf(Boolean condition) {
        if (!condition) {
            P paragraph = getParagraph();
            paragraphsToBeRemoved.add(paragraph);
        }
    }

    @Override
    public void displayParagraphIfPresent(Object condition) {
        displayParagraphIf(condition != null);
    }

    @Override
    public void displayTableIf(Boolean condition) {
        if (!condition) {
            P paragraph = getParagraph();
            if (paragraph.getParent() instanceof Tc &&
                    ((Tc) paragraph.getParent()).getParent() instanceof Tr &&
                    ((Tr)((Tc) paragraph.getParent()).getParent()).getParent() instanceof Tbl) {
                tablesToBeRemoved.add(((Tbl)((Tr)((Tc) paragraph.getParent()).getParent()).getParent()));
            } else {
                throw new CommentProcessingException("Paragraph is not within a table!", paragraph);
            }
        }
    }

    @Override
    public void displayTableRowIf(Boolean condition) {
        if (!condition) {
            P paragraph = getParagraph();
            if (paragraph.getParent() instanceof Tc &&
                    ((Tc) paragraph.getParent()).getParent() instanceof Tr) {
                tableRowsToBeRemoved.add((Tr)((Tc) paragraph.getParent()).getParent());
            } else {
                throw new CommentProcessingException("Paragraph is not within a table!", paragraph);
            }
        }
    }
}
