package org.wickedsource.docxstamper.processor.table;

import jakarta.xml.bind.JAXBElement;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.*;
import org.wickedsource.docxstamper.api.commentprocessor.ICommentProcessor;
import org.wickedsource.docxstamper.processor.BaseCommentProcessor;
import org.wickedsource.docxstamper.processor.CommentProcessingException;
import org.wickedsource.docxstamper.replace.PlaceholderReplacer;
import org.wickedsource.docxstamper.util.ParagraphUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * <p>TableResolver class.</p>
 *
 * @author joseph
 * @version $Id: $Id
 */
public class TableResolver extends BaseCommentProcessor implements ITableResolver {
	private final Map<Tbl, StampTable> cols = new HashMap<>();
	private final Function<Tbl, List<Object>> nullSupplier;

	private TableResolver(PlaceholderReplacer placeholderReplacer,
						  Function<Tbl, List<Object>> nullSupplier) {
		super(placeholderReplacer);
		this.nullSupplier = nullSupplier;
	}

    /**
     * Generate a new {@link org.wickedsource.docxstamper.processor.table.TableResolver} instance
     *
     * @param pr                   a {@link org.wickedsource.docxstamper.replace.PlaceholderReplacer} instance
     * @param nullReplacementValue in case the value to interpret is <code>null</code>
     * @return a new {@link org.wickedsource.docxstamper.processor.table.TableResolver} instance
     */
	public static ICommentProcessor newInstance(PlaceholderReplacer pr, String nullReplacementValue) {
		return new TableResolver(pr, table -> List.of(ParagraphUtil.create(nullReplacementValue)));
    }

    /**
     * Generate a new {@link org.wickedsource.docxstamper.processor.table.TableResolver} instance where value is replaced by an empty list when <code>null</code>
     *
     * @param pr a {@link org.wickedsource.docxstamper.replace.PlaceholderReplacer} instance
     * @return a new {@link org.wickedsource.docxstamper.processor.table.TableResolver} instance
	 */
	public static ICommentProcessor newInstance(PlaceholderReplacer pr) {
        return new TableResolver(pr, table -> Collections.emptyList());
    }

    /** {@inheritDoc} */
	@Override
	public void resolveTable(StampTable givenTable) {
		P p = getParagraph();
		if (p.getParent() instanceof Tc tc && tc.getParent() instanceof Tr tr) {
			Tbl table = (Tbl) tr.getParent();
			cols.put(table, givenTable);
		}
		throw new CommentProcessingException("Paragraph is not within a table!", p);
    }

    /** {@inheritDoc} */
	@Override
	public void commitChanges(WordprocessingMLPackage document) {
		for (Map.Entry<Tbl, StampTable> entry : cols.entrySet()) {
			Tbl wordTable = entry.getKey();

			StampTable stampedTable = entry.getValue();

			if (stampedTable != null) {
				replaceTableInplace(wordTable, stampedTable);
			} else {
				List<Object> tableParentContent = ((ContentAccessor) wordTable.getParent()).getContent();
				int tablePosition = tableParentContent.indexOf(wordTable);
				List<Object> toInsert = nullSupplier.apply(wordTable);
				tableParentContent.set(tablePosition, toInsert);
			}
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

	private void setCellText(Tc tableCell, String content) {
		tableCell.getContent().clear();
		tableCell.getContent().add(ParagraphUtil.create(content));
    }

    /** {@inheritDoc} */
	@Override
	public void reset() {
		cols.clear();
	}
}
