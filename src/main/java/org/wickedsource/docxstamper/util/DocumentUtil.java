package org.wickedsource.docxstamper.util;

import jakarta.xml.bind.JAXBElement;
import org.docx4j.dml.Graphic;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.Drawing;
import org.docx4j.wml.R;
import org.wickedsource.docxstamper.api.DocxStamperException;
import org.wickedsource.docxstamper.replace.typeresolver.image.ImageResolver;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class DocumentUtil {
    private DocumentUtil() {
        throw new DocxStamperException("Utility clases shouldn't be instantiated");
    }

    /**
     * Recursively walk through the content accessor to replace embedded images and import the matching
     * files to the destination document before importing content.
     *
     * @param sourceDocument document to import.
     * @param destDocument   document to add the source document content to.
     * @return the whole content of the source document with imported images replaced.
     * @throws Exception
     */
    public static List<Object> prepareDocumentForInsert(WordprocessingMLPackage sourceDocument, WordprocessingMLPackage destDocument) throws Exception {
        return walkObjects(sourceDocument.getMainDocumentPart(), sourceDocument, destDocument);
    }

    /**
     * Recursively walk through the content accessor to replace embedded images and import the matching
     * files to the destination document.
     *
     * @param sourceContainer source container to walk.
     * @param sourceDocument  source document containing image files.
     * @param destDocument    destination document to add image files to.
     * @return the list of imported objects from the source container.
     * @throws Exception
     */
    private static List<Object> walkObjects(ContentAccessor sourceContainer, WordprocessingMLPackage sourceDocument, WordprocessingMLPackage destDocument) throws Exception {
        List<Object> result = new ArrayList<>();
        for (Object obj : sourceContainer.getContent()) {
            if (obj instanceof R && isImageRun((R) obj)) {
                byte[] imageData = getRunDrawingData((R) obj, sourceDocument);
                // TODO : retrieve filename, altText and width from source document
                result.add(ImageResolver.createRunWithImage(destDocument, imageData, null, null, null));
            } else if (obj instanceof ContentAccessor) {
                List<Object> importedChildren = walkObjects((ContentAccessor) obj, sourceDocument, destDocument);
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
     * Extract an image bytes from an embedded image run.
     *
     * @param run      run containing the embedded drawing.
     * @param document document to read the file from.
     * @return
     * @throws Docx4JException
     * @throws IOException
     */
    private static byte[] getRunDrawingData(R run, WordprocessingMLPackage document) throws Docx4JException, IOException {
        for (Object runElement : run.getContent()) {
            if (runElement instanceof JAXBElement && ((JAXBElement<?>) runElement).getValue() instanceof Drawing) {
                Drawing drawing = (Drawing) ((JAXBElement<?>) runElement).getValue();
                return getImageData(document, drawing);
            }
        }
        throw new DocxStamperException("Run drawing not found !");
    }

    /**
     * Retrieve an image bytes from the document part store.
     *
     * @param document document to read the file from.
     * @param drawing  drawing embedding the image.
     * @return
     * @throws IOException
     * @throws Docx4JException
     */
    private static byte[] getImageData(WordprocessingMLPackage document, Drawing drawing) throws IOException, Docx4JException {
        String imageRelId = getImageRelationshipId(drawing);
        Part imageRelPart = document.getMainDocumentPart().getRelationshipsPart().getPart(imageRelId);
        // TODO : find a better way to find image rel part name in source part store
        String imageRelPartName = imageRelPart.getPartName().getName().substring(1);
        return streamToByteArray(
                document.getSourcePartStore().getPartSize(imageRelPartName),
                document.getSourcePartStore().loadPart(imageRelPartName)
        );
    }

    /**
     * Check if a run contains an embedded image.
     *
     * @param run
     * @return true if the run contains an image, false otherwise.
     */
    private static boolean isImageRun(R run) {
        for (Object runElement : run.getContent()) {
            if (runElement instanceof JAXBElement && ((JAXBElement<?>) runElement).getValue() instanceof Drawing) {
                return true;
            }
        }
        return false;
    }

    /**
     * Retrieve an embedded drawing relationship id.
     *
     * @param drawing the drawing to get the relationship id.
     * @return
     */
    private static String getImageRelationshipId(Drawing drawing) {
        Graphic graphic = getInlineGraphic(drawing);
        return graphic.getGraphicData().getPic().getBlipFill().getBlip().getEmbed();
    }

    /**
     * Extract an inline graphic from a drawing.
     *
     * @param drawing the drawing containing the graphic.
     * @return
     */
    private static Graphic getInlineGraphic(Drawing drawing) {
        if (drawing.getAnchorOrInline().isEmpty()) {
            throw new DocxStamperException("Anchor or Inline is empty !");
        }
        Object anchorOrInline = drawing.getAnchorOrInline().get(0);
        if (anchorOrInline instanceof Inline) {
            Inline inline = ((Inline) anchorOrInline);
            return inline.getGraphic();
        } else {
            throw new DocxStamperException("Don't know how to process anchor !");
        }
    }

    /**
     * Converts an InputStream to byte array.
     *
     * @param size expected size of the byte array.
     * @param is   input stream to read data from.
     * @return the data from the input stream.
     * @throws IOException
     */
    private static byte[] streamToByteArray(long size, InputStream is) throws IOException {
        if (size > Integer.MAX_VALUE) {
            throw new DocxStamperException("Image size exceeds maximum allowed (2GB)");
        }
        int intSize = (int) size;
        byte[] data = new byte[intSize];
        int offset = 0;
        int numRead;
        while ((numRead = is.read(data, offset, intSize - offset)) > 0) {
            offset += numRead;
        }
        is.close();
        return data;
    }
}
