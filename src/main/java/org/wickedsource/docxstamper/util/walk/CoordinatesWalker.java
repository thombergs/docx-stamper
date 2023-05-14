package org.wickedsource.docxstamper.util.walk;


import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.utils.TraversalUtilVisitor;
import org.docx4j.wml.P;
import org.docx4j.wml.R;

public abstract class CoordinatesWalker
		extends TraversalUtilVisitor<P> {
	private final WordprocessingMLPackage document;

	public CoordinatesWalker(WordprocessingMLPackage document) {
		this.document = document;
	}

	public void walk() {
		var mainDocumentPart = document.getMainDocumentPart();
		var mainRelationshipPart = mainDocumentPart.getRelationshipsPart();
		TraversalUtil.visit(mainDocumentPart, this);
		mainRelationshipPart.getRelationshipsByType(Namespaces.HEADER)
							.stream()
							.map(mainRelationshipPart::getPart)
							.forEach(part -> TraversalUtil.visit(part, this));
		mainRelationshipPart.getRelationshipsByType(Namespaces.FOOTER)
							.stream()
							.map(mainRelationshipPart::getPart)
							.forEach(part -> TraversalUtil.visit(part, this));
	}

	@Override
	public void apply(P paragraph) {
		for (Object contentElement : paragraph.getContent()) {
			if (XmlUtils.unwrap(contentElement) instanceof R) {
				R run = (R) contentElement;
				onRun(run, paragraph);
			}
		}
		// we run the paragraph afterward so that the comments inside work before the whole paragraph comments
		onParagraph(paragraph);
	}

	protected abstract void onRun(R run, P paragraph);

	protected abstract void onParagraph(P paragraph);
}
