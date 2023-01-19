package org.wickedsource.docxstamper.processor.table;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Tr;
import org.wickedsource.docxstamper.DocxStamperConfiguration;
import org.wickedsource.docxstamper.api.coordinates.ParagraphCoordinates;
import org.wickedsource.docxstamper.api.coordinates.TableCellCoordinates;
import org.wickedsource.docxstamper.api.coordinates.TableCoordinates;
import org.wickedsource.docxstamper.api.coordinates.TableRowCoordinates;
import org.wickedsource.docxstamper.api.typeresolver.TypeResolverRegistry;
import org.wickedsource.docxstamper.processor.BaseCommentProcessor;
import org.wickedsource.docxstamper.processor.CommentProcessingException;
import org.wickedsource.docxstamper.util.ParagraphUtil;

import javax.xml.bind.JAXBElement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TableResolver extends BaseCommentProcessor implements ITableResolver {
    private final Map<Tbl, StampTable> cols = new HashMap<>();

    public TableResolver(DocxStamperConfiguration config, TypeResolverRegistry typeResolverRegistry) {
        super(config, typeResolverRegistry);
    }

    @Override
    public void resolveTable(StampTable givenTable) {
        ParagraphCoordinates pCoords = getCurrentParagraphCoordinates();
        if (pCoords.getParentTableCellCoordinates() == null ||
                pCoords.getParentTableCellCoordinates().getParentTableRowCoordinates() == null) {
            throw new CommentProcessingException("Paragraph is not within a table!", pCoords);
        }

        TableCellCoordinates parentTableCellCoordinates = pCoords.getParentTableCellCoordinates();
        TableRowCoordinates parentTableRowCoordinates = parentTableCellCoordinates.getParentTableRowCoordinates();
        TableCoordinates parentTableCoordinates = parentTableRowCoordinates.getParentTableCoordinates();
        Tbl table = parentTableCoordinates.getTable();

        cols.put(table, givenTable);
    }

    @Override
    public void commitChanges(WordprocessingMLPackage document) {
        for (Map.Entry<Tbl, StampTable> entry : cols.entrySet()) {
            Tbl wordTable = entry.getKey();

            StampTable stampedTable = entry.getValue();

            if (stampedTable != null) {
                replaceTableInplace(wordTable, stampedTable);
            } else if (configuration.isReplaceNullValues() && configuration.getNullValuesDefault() != null) {
                replaceTableWithNullDefault(wordTable);
            } else {
                removeTableFromDocument(wordTable);
            }
        }
    }

    private static void removeTableFromDocument(Tbl wordTable) {
        ((ContentAccessor) wordTable.getParent()).getContent().remove(wordTable);
    }

    private void replaceTableWithNullDefault(Tbl wordTable) {
        ContentAccessor accessor = ((ContentAccessor) wordTable.getParent());
        int tablePosition = accessor.getContent().indexOf(wordTable);
        if (tablePosition >= 0) {
            accessor.getContent().set(tablePosition, ParagraphUtil.create(configuration.getNullValuesDefault()));
        }
    }

    private void replaceTableInplace(Tbl wordTable, StampTable stampedTable) {
        List<String> stampedHeaders = stampedTable.headers();
        List<List<String>> stampedRecords = stampedTable.records();

        List<Object> rows = wordTable.getContent();
        Tr headerRow = (Tr) rows.get(0);
        Tr firstDataRow = (Tr) rows.get(1);

        growAndFillRow(headerRow, stampedHeaders);

        if (!stampedRecords.isEmpty()) {
            growAndFillRow(firstDataRow, stampedRecords.get(0));

            for (List<String> rowContent : stampedRecords.subList(1, stampedRecords.size())) {
                rows.add(copyRowFromTemplate(firstDataRow, rowContent));
            }
        } else {
            rows.remove(firstDataRow);
        }
    }

    private Tr copyRowFromTemplate(Tr firstDataRow, List<String> rowContent) {
        Tr newXmlRow = XmlUtils.deepCopy(firstDataRow);
        List<Object> xmlRow = newXmlRow.getContent();
        for (int i = 0; i < rowContent.size(); i++) {
            String cellContent = rowContent.get(i);
            Tc xmlCell = ((JAXBElement<Tc>) xmlRow.get(i)).getValue();
            setCellText(xmlCell, cellContent);
        }
        return newXmlRow;
    }

    private void growAndFillRow(Tr row, List<String> values) {
        List<Object> cellRowContent = row.getContent();

        //Replace text in first cell
        JAXBElement<Tc> cell0 = (JAXBElement<Tc>) cellRowContent.get(0);
        Tc cell0tc = cell0.getValue();
        setCellText(cell0tc, values.isEmpty() ? "" : values.get(0));

        if (values.size() > 1) {
            //Copy first cell and replace content for each remaining values
            for (String cellContent : values.subList(1, values.size())) {
                JAXBElement<Tc> xmlCell = XmlUtils.deepCopy(cell0);
                setCellText(xmlCell.getValue(), cellContent);
                cellRowContent.add(xmlCell);
            }
        }
    }

    private void setCellText(Tc tableCell, String content) {
        tableCell.getContent().clear();
        tableCell.getContent().add(ParagraphUtil.create(content));
    }

    @Override
    public void reset() {
        cols.clear();
    }
}
