package org.wickedsource.docxstamper.util.walk;

import org.docx4j.XmlUtils;
import org.docx4j.wml.*;

/**
 * This class walks the document and calls abstract methods for each element it encounters.
 * The following elements are supported:
 * <ul>
 * <li>{@link org.docx4j.wml.P}</li>
 * <li>{@link org.docx4j.wml.R}</li>
 * <li>{@link org.docx4j.wml.Tbl}</li>
 * <li>{@link org.docx4j.wml.Tr}</li>
 * <li>{@link org.docx4j.wml.Tc}</li>
 * <li>{@link org.docx4j.wml.CommentRangeStart}</li>
 * <li>{@link org.docx4j.wml.CommentRangeEnd}</li>
 * <li>{@link org.docx4j.wml.R.CommentReference}</li>
 * </ul>
 * The following elements are not supported:
 * <ul>
 * <li>{@link org.docx4j.wml.SdtBlock}</li>
 * <li>{@link org.docx4j.wml.SdtRun}</li>
 * <li>{@link org.docx4j.wml.SdtElement}</li>
 * <li>{@link org.docx4j.wml.CTSimpleField}</li>
 * <li>{@link org.docx4j.wml.CTSdtCell}</li>
 * <li>{@link org.docx4j.wml.CTSdtContentCell}</li>
 *
 * @author joseph
 * @version $Id: $Id
 */
public abstract class DocumentWalker {

	private final ContentAccessor contentAccessor;

    /**
     * Creates a new DocumentWalker that will traverse the given document.
     *
     * @param contentAccessor the document to traverse.
     */
    protected DocumentWalker(ContentAccessor contentAccessor) {
		this.contentAccessor = contentAccessor;
    }

    /**
     * Starts the traversal of the document.
     */
	public void walk() {
		for (Object contentElement : contentAccessor.getContent()) {
			Object unwrappedObject = XmlUtils.unwrap(contentElement);
			if (unwrappedObject instanceof P p) {
				walkParagraph(p);
			} else if (unwrappedObject instanceof R r) {
				walkRun(r);
			} else if (unwrappedObject instanceof Tbl table) {
				walkTable(table);
			} else if (unwrappedObject instanceof Tr row) {
				walkTableRow(row);
			} else if (unwrappedObject instanceof Tc cell) {
				walkTableCell(cell);
			} else if (unwrappedObject instanceof CommentRangeStart commentRangeStart) {
				onCommentRangeStart(commentRangeStart);
			} else if (unwrappedObject instanceof CommentRangeEnd commentRangeEnd) {
				onCommentRangeEnd(commentRangeEnd);
			} else if (unwrappedObject instanceof R.CommentReference commentReference) {
				onCommentReference(commentReference);
			}
		}
	}

	private void walkTable(Tbl table) {
		onTable(table);
		for (Object contentElement : table.getContent()) {
			Object unwrappedObject = XmlUtils.unwrap(contentElement);
			if (unwrappedObject instanceof Tr row) {
				walkTableRow(row);
			}
		}
	}

	private void walkTableRow(Tr row) {
		onTableRow(row);
		for (Object rowContentElement : row.getContent()) {
			Object unwrappedObject = XmlUtils.unwrap(rowContentElement);
			if (unwrappedObject instanceof Tc cell) {
				walkTableCell(cell);
			}
		}
	}

	private void walkTableCell(Tc cell) {
		onTableCell(cell);
		for (Object cellContentElement : cell.getContent()) {
			Object unwrappedObject = XmlUtils.unwrap(cellContentElement);
			if (unwrappedObject instanceof P) {
				P p = (P) cellContentElement;
				walkParagraph(p);
			} else if (unwrappedObject instanceof R) {
				R r = (R) cellContentElement;
				walkRun(r);
			} else if (unwrappedObject instanceof Tbl nestedTable) {
				walkTable(nestedTable);
			} else if (unwrappedObject instanceof CommentRangeStart commentRangeStart) {
				onCommentRangeStart(commentRangeStart);
			} else if (unwrappedObject instanceof CommentRangeEnd commentRangeEnd) {
				onCommentRangeEnd(commentRangeEnd);
			}
		}
	}

	private void walkParagraph(P p) {
		onParagraph(p);
		for (Object element : p.getContent()) {
			Object unwrappedObject = XmlUtils.unwrap(element);
			if (unwrappedObject instanceof R r) {
				walkRun(r);
			} else if (unwrappedObject instanceof CommentRangeStart commentRangeStart) {
				onCommentRangeStart(commentRangeStart);
			} else if (unwrappedObject instanceof CommentRangeEnd commentRangeEnd) {
				onCommentRangeEnd(commentRangeEnd);
			}
		}
	}

	private void walkRun(R r) {
		onRun(r);
		for (Object element : r.getContent()) {
			Object unwrappedObject = XmlUtils.unwrap(element);
			if (unwrappedObject instanceof R.CommentReference commentReference) {
				onCommentReference(commentReference);
            }
        }
    }

    /**
     * This method is called for every {@link org.docx4j.wml.R} element in the document.
     *
     * @param run the {@link org.docx4j.wml.R} element to process.
     */
	protected abstract void onRun(R run);

    /**
     * This method is called for every {@link org.docx4j.wml.P} element in the document.
     *
     * @param paragraph the {@link org.docx4j.wml.P} element to process.
     */
	protected abstract void onParagraph(P paragraph);

    /**
     * This method is called for every {@link org.docx4j.wml.Tbl} element in the document.
     *
     * @param table the {@link org.docx4j.wml.Tbl} element to process.
     */
	protected abstract void onTable(Tbl table);

    /**
     * This method is called for every {@link org.docx4j.wml.Tc} element in the document.
     *
     * @param tableCell the {@link org.docx4j.wml.Tc} element to process.
     */
	protected abstract void onTableCell(Tc tableCell);

    /**
     * This method is called for every {@link org.docx4j.wml.Tr} element in the document.
     *
     * @param tableRow the {@link org.docx4j.wml.Tr} element to process.
     */
	protected abstract void onTableRow(Tr tableRow);

    /**
     * This method is called for every {@link org.docx4j.wml.CommentRangeStart} element in the document.
     *
     * @param commentRangeStart the {@link org.docx4j.wml.CommentRangeStart} element to process.
     */
	protected abstract void onCommentRangeStart(CommentRangeStart commentRangeStart);

    /**
     * This method is called for every {@link org.docx4j.wml.CommentRangeEnd} element in the document.
     *
     * @param commentRangeEnd the {@link org.docx4j.wml.CommentRangeEnd} element to process.
     */
	protected abstract void onCommentRangeEnd(CommentRangeEnd commentRangeEnd);

    /**
     * This method is called for every {@link org.docx4j.wml.R.CommentReference} element in the document.
     *
     * @param commentReference the {@link org.docx4j.wml.R.CommentReference} element to process.
     */
	protected abstract void onCommentReference(R.CommentReference commentReference);
}
