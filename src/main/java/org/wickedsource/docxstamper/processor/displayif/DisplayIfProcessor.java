package org.wickedsource.docxstamper.processor.displayif;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Tr;
import org.wickedsource.docxstamper.api.commentprocessor.ICommentProcessor;
import org.wickedsource.docxstamper.processor.BaseCommentProcessor;
import org.wickedsource.docxstamper.processor.CommentProcessingException;
import org.wickedsource.docxstamper.replace.PlaceholderReplacer;
import org.wickedsource.docxstamper.util.ObjectDeleter;

import java.util.ArrayList;
import java.util.List;

/**
 * Processor for the {@link org.wickedsource.docxstamper.processor.displayif.IDisplayIfProcessor} comment.
 *
 * @author joseph
 * @version $Id: $Id
 */
public class DisplayIfProcessor extends BaseCommentProcessor implements IDisplayIfProcessor {

	private List<P> paragraphsToBeRemoved = new ArrayList<>();
	private List<Tbl> tablesToBeRemoved = new ArrayList<>();
	private List<Tr> tableRowsToBeRemoved = new ArrayList<>();

	private DisplayIfProcessor(PlaceholderReplacer placeholderReplacer) {
		super(placeholderReplacer);
	}

	/**
	 * Creates a new DisplayIfProcessor instance.
	 *
	 * @param pr the {@link org.wickedsource.docxstamper.replace.PlaceholderReplacer} to use for replacing placeholders.
	 * @return a new DisplayIfProcessor instance.
	 */
	public static ICommentProcessor newInstance(PlaceholderReplacer pr) {
		return new DisplayIfProcessor(pr);
	}

	/** {@inheritDoc} */
	@Override
	public void commitChanges(WordprocessingMLPackage document) {
        removeParagraphs();
        removeTables();
        removeTableRows();
	}

	/** {@inheritDoc} */
	@Override
	public void reset() {
		paragraphsToBeRemoved = new ArrayList<>();
		tablesToBeRemoved = new ArrayList<>();
		tableRowsToBeRemoved = new ArrayList<>();
    }

    private void removeParagraphs() {
		for (P p : paragraphsToBeRemoved) {
            ObjectDeleter.deleteParagraph(p);
        }
    }

    private void removeTables() {
		for (Tbl table : tablesToBeRemoved) {
            ObjectDeleter.deleteTable(table);
        }
    }

    private void removeTableRows() {
		for (Tr row : tableRowsToBeRemoved) {
            ObjectDeleter.deleteTableRow(row);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void displayParagraphIf(Boolean condition) {
		if (Boolean.TRUE.equals(condition)) return;
		paragraphsToBeRemoved.add(getParagraph());
	}

	/** {@inheritDoc} */
	@Override
	public void displayParagraphIfPresent(Object condition) {
		displayParagraphIf(condition != null);
	}

	/** {@inheritDoc} */
	@Override
	public void displayTableIf(Boolean condition) {
		if (Boolean.TRUE.equals(condition)) return;

		P p = getParagraph();
		if (p.getParent() instanceof Tc tc
				&& tc.getParent() instanceof Tr tr
				&& tr.getParent() instanceof Tbl tbl
		) {
			tablesToBeRemoved.add(tbl);
		} else {
			throw new CommentProcessingException("Paragraph is not within a table!", p);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void displayTableRowIf(Boolean condition) {
		if (Boolean.TRUE.equals(condition)) return;

		P p = getParagraph();
		if (p.getParent() instanceof Tc tc
				&& tc.getParent() instanceof Tr tr) {
			tableRowsToBeRemoved.add(tr);
		} else {
			throw new CommentProcessingException("Paragraph is not within a table!", p);
		}
	}
}
