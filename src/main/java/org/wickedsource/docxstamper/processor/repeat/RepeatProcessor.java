package org.wickedsource.docxstamper.processor.repeat;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.docx4j.wml.Tr;
import org.wickedsource.docxstamper.DocxStamperConfiguration;
import org.wickedsource.docxstamper.api.coordinates.ParagraphCoordinates;
import org.wickedsource.docxstamper.api.coordinates.TableRowCoordinates;
import org.wickedsource.docxstamper.api.typeresolver.TypeResolverRegistry;
import org.wickedsource.docxstamper.processor.BaseCommentProcessor;
import org.wickedsource.docxstamper.processor.CommentProcessingException;
import org.wickedsource.docxstamper.util.CommentUtil;
import org.wickedsource.docxstamper.util.CommentWrapper;
import org.wickedsource.docxstamper.util.walk.BaseDocumentWalker;
import org.wickedsource.docxstamper.util.walk.DocumentWalker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RepeatProcessor extends BaseCommentProcessor implements IRepeatProcessor {

    private Map<TableRowCoordinates, List<Object>> tableRowsToRepeat = new HashMap<>();
    private Map<TableRowCoordinates, CommentWrapper> tableRowsCommentsToRemove = new HashMap<>();

    public RepeatProcessor(DocxStamperConfiguration config, TypeResolverRegistry typeResolverRegistry) {
        super(config, typeResolverRegistry);
    }


    @Override
    public void commitChanges(WordprocessingMLPackage document) {
        repeatRows(document);
    }

    @Override
    public void reset() {
        this.tableRowsToRepeat = new HashMap<>();
        this.tableRowsCommentsToRemove = new HashMap<>();
    }

    private void repeatRows(final WordprocessingMLPackage document) {
        for (TableRowCoordinates rCoords : tableRowsToRepeat.keySet()) {
            List<Object> expressionContexts = tableRowsToRepeat.get(rCoords);
            int index = rCoords.getIndex();

            if (expressionContexts != null) {
                for (final Object expressionContext : expressionContexts) {
                    Tr rowClone = XmlUtils.deepCopy(rCoords.getRow());
                    CommentUtil.deleteCommentFromElement(rowClone, tableRowsCommentsToRemove.get(rCoords).getComment().getId());

                    DocumentWalker walker = new BaseDocumentWalker(rowClone) {
                        @Override
                        protected void onParagraph(P paragraph) {
                            placeholderReplacer.resolveExpressionsForParagraph(paragraph, expressionContext, document);
                        }
                    };
                    walker.walk();
                    rCoords.getParentTableCoordinates().getTable().getContent().add(++index, rowClone);
                }
            }

            // TODO : how to replace null values here ?
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
        TableRowCoordinates parentTableRowCoordinates = getCurrentParagraphCoordinates().getParentTableCellCoordinates().getParentTableRowCoordinates();
        tableRowsToRepeat.put(parentTableRowCoordinates, objects);
        tableRowsCommentsToRemove.put(parentTableRowCoordinates, getCurrentCommentWrapper());
    }
}
