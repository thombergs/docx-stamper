package org.wickedsource.docxstamper.processor.repeat;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Body;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.P;
import org.docx4j.wml.Tbl;
import org.wickedsource.docxstamper.api.coordinates.ParagraphCoordinates;
import org.wickedsource.docxstamper.api.coordinates.TableCoordinates;
import org.wickedsource.docxstamper.api.typeresolver.TypeResolverRegistry;
import org.wickedsource.docxstamper.el.ExpressionResolver;
import org.wickedsource.docxstamper.processor.BaseCommentProcessor;
import org.wickedsource.docxstamper.processor.CommentProcessingException;
import org.wickedsource.docxstamper.replace.PlaceholderReplacer;
import org.wickedsource.docxstamper.util.walk.BaseDocumentWalker;
import org.wickedsource.docxstamper.util.walk.DocumentWalker;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RepeatTable extends BaseCommentProcessor implements IRepeatTable {

    private Map<TableCoordinates, List<Object>> tableToRepeat = new HashMap<>();

    private PlaceholderReplacer<Object> placeholderReplacer;

    public RepeatTable(TypeResolverRegistry typeResolverRegistry, ExpressionResolver expressionResolver) {
        this.placeholderReplacer = new PlaceholderReplacer<>(typeResolverRegistry);
        this.placeholderReplacer.setExpressionResolver(expressionResolver);
    }

    @Override
    public void commitChanges(WordprocessingMLPackage document) {
        repeatTables(document);
    }

    @Override
    public void reset() {
        this.tableToRepeat = new HashMap<>();
    }

    private void repeatTables(final WordprocessingMLPackage document) {
        for (TableCoordinates rCoords : tableToRepeat.keySet()) {
            List<Object> expressionContexts = tableToRepeat.get(rCoords);
            int index = rCoords.getIndex();
            List<Object> content;
            //get public content
            if (rCoords.getTable().getParent() instanceof JAXBElement) {
                content = document.getMainDocumentPart().getContent();
            } else {
                content = ((ContentAccessor) rCoords.getTable().getParent()).getContent();
            }
            for (final Object expressionContext : expressionContexts) {
                Tbl tblClone = XmlUtils.deepCopy(rCoords.getTable());

                DocumentWalker walker = new BaseDocumentWalker(tblClone) {
                    @Override
                    protected void onParagraph(P paragraph) {
                        placeholderReplacer.resolveExpressionsForParagraph(paragraph, expressionContext, document);
                    }
                };

                walker.walk();

                // add Table Node and P Node
                if (rCoords.getTable().getParent() instanceof JAXBElement) {
                    QName tblQName = new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "tbl");
                    JAXBElement<Tbl> tblJAXBElement = new JAXBElement<>(tblQName, Tbl.class, Body.class, tblClone);
                    content.add(++index, tblJAXBElement);
                } else {
                    content.add(++index, tblClone);
                }
                content.add(++index, new P());

            }
            content.remove(rCoords.getIndex());
            // TODO: remove "repeatTableRow"-comment from cloned rows!
        }
    }


    @Override
    public void repeatTable(List<Object> objects) {
        ParagraphCoordinates pCoords = getCurrentParagraphCoordinates();
        if (pCoords.getParentTableCellCoordinates() == null ||
                pCoords.getParentTableCellCoordinates().getParentTableRowCoordinates() == null ||
                pCoords.getParentTableCellCoordinates().getParentTableRowCoordinates().getParentTableCoordinates() == null ||
                pCoords.getParentTableCellCoordinates().getParentTableRowCoordinates().getParentTableCoordinates().getTable() == null) {
            throw new CommentProcessingException("Paragraph is not within a table!", pCoords);
        }
        tableToRepeat.put(pCoords.getParentTableCellCoordinates().getParentTableRowCoordinates().getParentTableCoordinates(), objects);
    }

}
