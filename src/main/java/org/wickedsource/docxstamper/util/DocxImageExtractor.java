package org.wickedsource.docxstamper.util;

import jakarta.xml.bind.JAXBElement;
import org.docx4j.dml.Graphic;
import org.docx4j.dml.picture.Pic;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Drawing;
import org.docx4j.wml.R;
import org.wickedsource.docxstamper.api.DocxStamperException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class DocxImageExtractor {

    private final WordprocessingMLPackage wordprocessingMLPackage;

    public DocxImageExtractor(WordprocessingMLPackage wordprocessingMLPackage) {
        this.wordprocessingMLPackage = wordprocessingMLPackage;
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
     *
     */
    private static byte[] streamToByteArray(long size, InputStream is) throws IOException {
        if (size > Integer.MAX_VALUE) {
            throw new DocxStamperException("Image size exceeds maximum allowed (2GB)");
        }
        int intSize = (int) size;
        byte[] data = new byte[intSize];
        int numRead = is.read(data);
        return Arrays.copyOfRange(data, 0, numRead);
    }

    private static Pic getPic(R run) {
        for (Object runContent : run.getContent()) {
            if (!(runContent instanceof JAXBElement)) break;
            JAXBElement<?> runElement = (JAXBElement<?>) runContent;
            if (!(runElement.getValue() instanceof Drawing)) break;
            Drawing drawing = (Drawing) runElement.getValue();
            Graphic graphic = getInlineGraphic(drawing);
            return graphic.getGraphicData().getPic();
        }
        throw new DocxStamperException("Run drawing not found !");
    }

    private InputStream getImageStream(String imageRelPartName) throws Docx4JException {
        return wordprocessingMLPackage
                .getSourcePartStore()
                .loadPart(imageRelPartName);
    }

    private long getImageSize(String imageRelPartName) throws Docx4JException {
        return wordprocessingMLPackage
                .getSourcePartStore()
                .getPartSize(imageRelPartName);
    }

    private String getImageRelPartName(String imageRelId) {
        // TODO : find a better way to find image rel part name in source part store
        return wordprocessingMLPackage
                .getMainDocumentPart()
                .getRelationshipsPart()
                .getPart(imageRelId)
                .getPartName()
                .getName()
                .substring(1);
    }

    /**
     * Extract an image bytes from an embedded image run.
     *
     * @param run run containing the embedded drawing.
     * @return
     * @throws Docx4JException
     * @throws IOException
     */
    byte[] getRunDrawingData(R run) throws Docx4JException, IOException {
        String imageRelId = getPic(run).getBlipFill().getBlip().getEmbed();
        String imageRelPartName = getImageRelPartName(imageRelId);
        long size = getImageSize(imageRelPartName);
        InputStream stream = getImageStream(imageRelPartName);
        return streamToByteArray(size, stream);
    }

    public String getRunDrawingFilename(R run) {
        return getPic(run).getNvPicPr().getCNvPr().getName();
    }

    public String getRunDrawingAltText(R run) {
        return getPic(run).getNvPicPr().getCNvPr().getDescr();
    }

    public Integer getRunDrawingMaxWidth(R run) {
        return (int) getPic(run).getSpPr().getXfrm().getExt().getCx();
    }
}
