package org.wickedsource.docxstamper.util.walk;

import org.docx4j.XmlUtils;
import org.docx4j.wml.*;

public abstract class DocumentWalker {

	private final ContentAccessor contentAccessor;

	public DocumentWalker(ContentAccessor contentAccessor) {
		this.contentAccessor = contentAccessor;
	}

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

	protected abstract void onRun(R run);

	protected abstract void onParagraph(P paragraph);

	protected abstract void onTable(Tbl table);

	protected abstract void onTableCell(Tc tableCell);

	protected abstract void onTableRow(Tr tableRow);

	protected abstract void onCommentRangeStart(CommentRangeStart commentRangeStart);

	protected abstract void onCommentRangeEnd(CommentRangeEnd commentRangeEnd);

	protected abstract void onCommentReference(R.CommentReference commentReference);
}
