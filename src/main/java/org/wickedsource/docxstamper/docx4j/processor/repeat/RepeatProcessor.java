package org.wickedsource.docxstamper.docx4j.processor.repeat;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.docx4j.wml.Tr;
import org.wickedsource.docxstamper.docx4j.PlaceholderReplacer;
import org.wickedsource.docxstamper.docx4j.processor.BaseCommentProcessor;
import org.wickedsource.docxstamper.docx4j.processor.CommentProcessingException;
import org.wickedsource.docxstamper.docx4j.walk.BaseDocumentWalker;
import org.wickedsource.docxstamper.docx4j.walk.DocumentWalker;
import org.wickedsource.docxstamper.docx4j.walk.coordinates.ParagraphCoordinates;
import org.wickedsource.docxstamper.docx4j.walk.coordinates.TableRowCoordinates;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RepeatProcessor extends BaseCommentProcessor implements IRepeatProcessor {

    private Map<TableRowCoordinates, List<Object>> tableRowsToRepeat = new HashMap<>();

    private PlaceholderReplacer<Object> placeholderReplacer = new PlaceholderReplacer<>();

    @Override
    public void commitChanges(WordprocessingMLPackage document) {
        repeatRows();
    }

    private void repeatRows() {
        for (TableRowCoordinates rCoords : tableRowsToRepeat.keySet()) {
            List<Object> expressionContexts = tableRowsToRepeat.get(rCoords);
            for (final Object expressionContext : expressionContexts) {
                Tr rowClone = XmlUtils.deepCopy(rCoords.getRow());
                DocumentWalker walker = new BaseDocumentWalker(rowClone) {
                    @Override
                    protected void onParagraph(P paragraph) {
                        placeholderReplacer.resolveExpressionsForParagraph(paragraph, expressionContext);
                    }
                };
                walker.walk();
                rCoords.getParentTableCoordinates().getTable().getContent().add(rowClone);
            }
            rCoords.getParentTableCoordinates().getTable().getContent().remove(rCoords.getRow());
        }
    }


    @Override
    public void repeatTableRow(List<Object> objects) {
        ParagraphCoordinates pCoords = getCurrentParagraphCoordinates();
        if (pCoords.getParentTableCellCoordinates() == null ||
                pCoords.getParentTableCellCoordinates().getParentTableRowCoordinates() == null) {
            throw new CommentProcessingException("Paragraph is not within a table!", pCoords);
        }
        tableRowsToRepeat.put(getCurrentParagraphCoordinates().getParentTableCellCoordinates().getParentTableRowCoordinates(), objects);
    }

}
