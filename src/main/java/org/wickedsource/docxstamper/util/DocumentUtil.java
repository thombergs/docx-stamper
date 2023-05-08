package org.wickedsource.docxstamper.util;

import jakarta.xml.bind.JAXBElement;
import org.docx4j.TraversalUtil;
import org.docx4j.dml.Graphic;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.finders.ClassFinder;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.*;
import org.wickedsource.docxstamper.api.DocxStamperException;
import org.wickedsource.docxstamper.replace.typeresolver.image.ImageResolver;

import java.util.*;
import java.util.stream.Stream;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;

public class DocumentUtil {
	private DocumentUtil() {
		throw new DocxStamperException("Utility classes shouldn't be instantiated");
	}

	public static <T> List<T> extractElements(Object object, Class<T> elementClass) {
		// we handle full documents slightly differently as they have headers and footers
		if (object instanceof WordprocessingMLPackage document) {
			return Stream.of(
								 getElementStreamFrom(document, elementClass, Namespaces.HEADER),
								 getElementStream(document.getMainDocumentPart(), elementClass),
								 getElementStreamFrom(document, elementClass, Namespaces.FOOTER)
						 )
						 .flatMap(identity())
						 .collect(toList());
		}

		return getElementStream(object, elementClass)
				.collect(toList());
	}

	private static <T> Stream<T> getElementStreamFrom(
			WordprocessingMLPackage document,
			Class<T> clazz,
			String relationshipType
	) {
		RelationshipsPart relationshipsPart = document
				.getMainDocumentPart()
				.getRelationshipsPart();
		return relationshipsPart
				.getRelationships()
				.getRelationship()
				.stream()
				.filter(relationship -> relationship.getType().equals(relationshipType))
				.map(relationshipsPart::getPart)
				.flatMap(relationshipPart -> getElementStream(relationshipPart, clazz));
	}

	private static <T> Stream<T> getElementStream(Object obj, Class<T> clazz) {
		ClassFinder finder = new ClassFinder(clazz);
		finder.walkJAXBElements(obj);
		return finder.results
				.stream()
				.map(clazz::cast);
	}

	/**
	 * Retrieve an embedded drawing relationship id.
	 *
	 * @param drawing the drawing to get the relationship id.
	 * @return the id of the graphic
	 */
	public static String getImageRelationshipId(Drawing drawing) {
		Graphic graphic = getInlineGraphic(drawing);
		return graphic.getGraphicData().getPic().getBlipFill().getBlip().getEmbed();
	}

	/**
	 * Extract an inline graphic from a drawing.
	 *
	 * @param drawing the drawing containing the graphic.
	 * @return the graphic
	 */
	private static Graphic getInlineGraphic(Drawing drawing) {
		if (drawing.getAnchorOrInline().isEmpty()) {
			throw new RuntimeException("Anchor or Inline is empty !");
		}
		Object anchorOrInline = drawing.getAnchorOrInline().get(0);
		if (anchorOrInline instanceof Inline inline) {
			return inline.getGraphic();
		} else {
			throw new RuntimeException("Don't know how to process anchor !");
		}
	}

	public static List<P> getParagraphsFromObject(Object parentObject) {
		List<P> paragraphList = new ArrayList<>();
		for (Object object : getElementsFromObject(parentObject, P.class)) {
			if (object instanceof P) {
				paragraphList.add((P) object);
			}
		}
		return paragraphList;
	}

	private static List<Object> getElementsFromObject(Object object, Class<?> elementClass) {
		List<Object> documentElements = new ArrayList<>();
		// we handle full documents slightly differently as they have headers and footers
		if (object instanceof WordprocessingMLPackage) {
			documentElements.addAll(getElementsFromHeader(((WordprocessingMLPackage) object), elementClass));
			documentElements.addAll(getElements(((WordprocessingMLPackage) object).getMainDocumentPart(),
												elementClass));
			documentElements.addAll(getElementsForFooter(((WordprocessingMLPackage) object), elementClass));
		} else {
			documentElements.addAll(getElements(object, elementClass));
		}
		return documentElements;
	}

	private static List<Object> getElementsFromHeader(WordprocessingMLPackage document, Class<?> elementClass) {
		List<Object> paragraphs = new ArrayList<>();
		RelationshipsPart relationshipsPart = document.getMainDocumentPart().getRelationshipsPart();

		// walk through elements in headers
		List<Relationship> relationships = getRelationshipsOfType(document, Namespaces.HEADER);
		for (Relationship header : relationships) {
			HeaderPart headerPart = (HeaderPart) relationshipsPart.getPart(header.getId());
			paragraphs.addAll(getElements(headerPart, elementClass));
		}
		return paragraphs;
	}

	private static List<Object> getElements(Object obj, Class<?> elementClass) {
		ClassFinder finder = new ClassFinder(elementClass);
		TraversalUtil.visit(obj, finder);
		return finder.results;
	}

	private static List<Object> getElementsForFooter(WordprocessingMLPackage document, Class<?> elementClass) {
		List<Object> paragraphs = new ArrayList<>();
		RelationshipsPart relationshipsPart = document.getMainDocumentPart().getRelationshipsPart();

		// walk through elements in footers
		List<Relationship> relationships = getRelationshipsOfType(document, Namespaces.FOOTER);
		for (Relationship footer : relationships) {
			FooterPart footerPart = (FooterPart) relationshipsPart.getPart(footer.getId());
			paragraphs.addAll(getElements(footerPart, elementClass));
		}

		return paragraphs;
	}

	private static List<Relationship> getRelationshipsOfType(WordprocessingMLPackage document, String type) {
		List<Relationship> relationshipList = document
				.getMainDocumentPart()
				.getRelationshipsPart()
				.getRelationships()
				.getRelationship();
		List<Relationship> headerRelationships = new ArrayList<>();
		for (Relationship r : relationshipList) {
			if (r.getType().equals(type)) {
				headerRelationships.add(r);
			}
		}
		return headerRelationships;
	}

	public static List<Tbl> getTableFromObject(Object parentObject) {
		List<Tbl> tableList = new ArrayList<>();
		for (Object object : getElementsFromObject(parentObject, Tbl.class)) {
			if (object instanceof Tbl) {
				tableList.add((Tbl) object);
			}
		}
		return tableList;
	}

	public static List<Tr> getTableRowsFromObject(Object parentObject) {
		List<Tr> tableRowList = new ArrayList<>();
		for (Object object : getElementsFromObject(parentObject, Tr.class)) {
			if (object instanceof Tr) {
				tableRowList.add((Tr) object);
			}
		}
		return tableRowList;
	}

	public static List<Tc> getTableCellsFromObject(Object parentObject) {
		return getElementsFromObject(parentObject, Tc.class)
				.stream()
				.filter(object -> object instanceof Tc)
				.map(object -> (Tc) object)
				.toList();
	}

	public static Object lastElement(WordprocessingMLPackage subDocument) {
		List<Object> content = subDocument.getMainDocumentPart().getContent();
		return content.get(content.size() - 1);
	}

	public static List<Object> allElements(WordprocessingMLPackage subDocument) {
		return subDocument.getMainDocumentPart().getContent();
	}

	/**
	 * Recursively walk through source to find embedded images and import them in the target document.
	 *
	 * @param source source document containing image files.
	 * @param target target document to add image files to.
	 */
	public static Map<R, R> walkObjectsAndImportImages(WordprocessingMLPackage source, WordprocessingMLPackage target) {
		return walkObjectsAndImportImages(source.getMainDocumentPart(), source, target);
	}

	/**
	 * Recursively walk through source accessor to find embedded images and import the target document.
	 *
	 * @param container source container to walk.
	 * @param source    source document containing image files.
	 * @param target    target document to add image files to.
	 */
	public static Map<R, R> walkObjectsAndImportImages(
			ContentAccessor container,
			WordprocessingMLPackage source,
			WordprocessingMLPackage target
	) {
		Map<R, R> replacements = new HashMap<>();
		for (Object obj : container.getContent()) {
			Queue<Object> queue = new ArrayDeque<>();
			queue.add(obj);

			while (!queue.isEmpty()) {
				Object currentObj = queue.remove();

				if (currentObj instanceof R currentR && isImageRun(currentR)) {
					DocxImageExtractor docxImageExtractor = new DocxImageExtractor(source);
					byte[] imageData = docxImageExtractor.getRunDrawingData(currentR);
					String filename = docxImageExtractor.getRunDrawingFilename(currentR);
					String alt = docxImageExtractor.getRunDrawingAltText(currentR);
					Integer maxWidth = docxImageExtractor.getRunDrawingMaxWidth(currentR);
					BinaryPartAbstractImage imagePart = tryCreateImagePart(target, imageData);
					replacements.put(currentR, ImageResolver.createRunWithImage(filename, alt, maxWidth, imagePart));
				} else if (currentObj instanceof ContentAccessor contentAccessor)
					queue.addAll(contentAccessor.getContent());
			}
		}
		return replacements;
	}

	/**
	 * Check if a run contains an embedded image.
	 *
	 * @param run the run to analyze
	 * @return true if the run contains an image, false otherwise.
	 */
	private static boolean isImageRun(R run) {
		return run.getContent()
				  .stream()
				  .filter(runElement -> runElement instanceof JAXBElement)
				  .map(JAXBElement.class::cast)
				  .map(JAXBElement::getValue)
				  .anyMatch(runValue -> runValue instanceof Drawing);
	}

	private static BinaryPartAbstractImage tryCreateImagePart(WordprocessingMLPackage destDocument, byte[] imageData) {
		try {
			return BinaryPartAbstractImage.createImagePart(destDocument, imageData);
		} catch (Exception e) {
			throw new DocxStamperException(e);
		}
	}


}
