package org.wickedsource.docxstamper.util;

import jakarta.xml.bind.JAXBElement;
import org.docx4j.TraversalUtil;
import org.docx4j.dml.Graphic;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.finders.ClassFinder;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.*;
import org.wickedsource.docxstamper.api.DocxStamperException;
import org.wickedsource.docxstamper.replace.typeresolver.image.ImageResolver;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;

public class DocumentUtil {
    private DocumentUtil() {
        throw new DocxStamperException("Utility classes shouldn't be instantiated");
    }

    /**
     * Recursively walk through the content accessor to replace embedded images and import the matching
     * files to the destination document before importing content.
     *
     * @param sourceDocument document to import.
     * @param targetDocument document to add the source document content to.
     * @return the whole content of the source document with imported images replaced.
     */
    public static List<Object> prepareDocumentForInsert(
            WordprocessingMLPackage sourceDocument,
            WordprocessingMLPackage targetDocument
    ) throws Exception {
        return walkObjectsAndImportImages(sourceDocument.getMainDocumentPart(), sourceDocument, targetDocument);
    }

    /**
     * Recursively walk through the content accessor to replace embedded images and import the matching
     * files to the destination document.
     *
     * @param sourceContainer source container to walk.
     * @param sourceDocument  source document containing image files.
     * @param destDocument    destination document to add image files to.
     * @return the list of imported objects from the source container.
     */
    public static List<Object> walkObjectsAndImportImages(
            ContentAccessor sourceContainer,
            WordprocessingMLPackage sourceDocument,
            WordprocessingMLPackage destDocument
    ) throws Exception {
        List<Object> result = new ArrayList<>();
        for (Object obj : sourceContainer.getContent()) {
            if (obj instanceof R && isImageRun((R) obj)) {
                DocxImageExtractor docxImageExtractor = new DocxImageExtractor(sourceDocument);
                byte[] imageData = docxImageExtractor.getRunDrawingData((R) obj);
                String filename = docxImageExtractor.getRunDrawingFilename((R) obj);
                String alt = docxImageExtractor.getRunDrawingAltText((R) obj);
                Integer maxWidth = docxImageExtractor.getRunDrawingMaxWidth((R) obj);
                result.add(ImageResolver.createRunWithImage(destDocument, imageData, filename, alt, maxWidth));
            } else if (obj instanceof ContentAccessor) {
                List<Object> importedChildren = walkObjectsAndImportImages((ContentAccessor) obj, sourceDocument, destDocument);
                ((ContentAccessor) obj).getContent().clear();
                ((ContentAccessor) obj).getContent().addAll(importedChildren);
                result.add(obj);
            } else {
                result.add(obj);
            }
        }
        return result;
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

    public static <T> List<T> extractElements(Object object, Class<T> elementClass) {
        // we handle full documents slightly differently as they have headers and footers
        if (object instanceof WordprocessingMLPackage) {
            WordprocessingMLPackage document = (WordprocessingMLPackage) object;

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
        if (anchorOrInline instanceof Inline) {
            Inline inline = ((Inline) anchorOrInline);
            return inline.getGraphic();
        } else {
            throw new RuntimeException("Don't know how to process anchor !");
        }
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
            documentElements.addAll(getElementsFromHeader(((WordprocessingMLPackage) (object)), elementClass));
            documentElements.addAll(getElements(((WordprocessingMLPackage) (object)).getMainDocumentPart(), elementClass));
            documentElements.addAll(getElementsForFooter(((WordprocessingMLPackage) (object)), elementClass));
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

    private static List<Object> getElements(Object obj, Class<?> elementClass) {
        ClassFinder finder = new ClassFinder(elementClass);
        new TraversalUtil(obj, finder);
        return finder.results;
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
        List<Tc> tableCellList = new ArrayList<>();
        for (Object object : getElementsFromObject(parentObject, Tc.class)) {
            if (object instanceof Tc) {
                tableCellList.add((Tc) object);
            }
        }
        return tableCellList;
    }
}
