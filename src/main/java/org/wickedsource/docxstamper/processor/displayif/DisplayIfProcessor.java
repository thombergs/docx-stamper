package org.wickedsource.docxstamper.processor.displayif;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Tr;
import org.wickedsource.docxstamper.DocxStamperConfiguration;
import org.wickedsource.docxstamper.api.typeresolver.TypeResolverRegistry;
import org.wickedsource.docxstamper.processor.BaseCommentProcessor;
import org.wickedsource.docxstamper.processor.CommentProcessingException;
import org.wickedsource.docxstamper.util.ObjectDeleter;

import java.util.ArrayList;
import java.util.List;

public class DisplayIfProcessor extends BaseCommentProcessor implements IDisplayIfProcessor {

	private List<P> paragraphsToBeRemoved = new ArrayList<>();
	private List<Tbl> tablesToBeRemoved = new ArrayList<>();
	private List<Tr> tableRowsToBeRemoved = new ArrayList<>();
	private List<Object> objectsToBeRemoved = new ArrayList<>();

	public DisplayIfProcessor(DocxStamperConfiguration config, TypeResolverRegistry typeResolverRegistry) {
		super(config, typeResolverRegistry);
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
		for (P p : paragraphsToBeRemoved) {
			deleter.deleteParagraph(p);
		}
	}

	private void removeTables(ObjectDeleter deleter) {
		for (Tbl table : tablesToBeRemoved) {
			deleter.deleteTable(table);
		}
	}

	private void removeTableRows(ObjectDeleter deleter) {
		for (Tr row : tableRowsToBeRemoved) {
			deleter.deleteTableRow(row);
		}
	}

	private void removeObjects(ObjectDeleter deleter) {
		for (Object object : objectsToBeRemoved) {
			deleter.deleteObject(object);
		}
	}

	@Override
	public void displayParagraphIf(Boolean condition) {
		if (condition) return;
		paragraphsToBeRemoved.add(getParagraph());
	}

	@Override
	public void displayParagraphIfPresent(Object condition) {
		displayParagraphIf(condition != null);
	}

	@Override
	public void displayTableIf(Boolean condition) {
		if (condition) return;

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

	@Override
	public void displayTableRowIf(Boolean condition) {
		if (condition) return;

		P p = getParagraph();
		if (p.getParent() instanceof Tc tc
				&& tc.getParent() instanceof Tr tr) {
			tableRowsToBeRemoved.add(tr);
		} else {
			throw new CommentProcessingException("Paragraph is not within a table!", p);
		}
	}
}
