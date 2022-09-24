package org.wickedsource.docxstamper.replace.typeresolver.image;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This class describes an image which will be inserted into document.
 */
public class Image {

    private final byte[] imageBytes;

    private String filename;

    private String altText;

    private Integer maxWidth;

    /**
     * @param in - content of the image as InputStream
     */
    public Image(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        IOUtils.copy(in, out);
        this.imageBytes = out.toByteArray();
    }

    /**
     * @param in - content of the image as InputStream
     * @param maxWidth - max width of the image in twip
     */
    public Image(InputStream in, Integer maxWidth) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        IOUtils.copy(in, out);
        this.imageBytes = out.toByteArray();
        this.maxWidth = maxWidth;
    }

    /**
     * @param imageBytes - content of the image as array of the bytes
     */
    public Image(byte[] imageBytes) {
        this.imageBytes = imageBytes;
    }

    /**
     * @param imageBytes - content of the image as array of the bytes
     * @param maxWidth - max width of the image in twip
     */
    public Image(byte[] imageBytes, Integer maxWidth) {
        this.imageBytes = imageBytes;
        this.maxWidth = maxWidth;
    }

    public String getFilename() {
        return filename;
    }

    /**
     * @return the expected alternative text to display for user that can't see the image itself
     */
    public String getAltText() {
        return altText;
    }

    public byte[] getImageBytes() {
        return imageBytes;
    }

    /**
     * The expected max width for this image
     *
     * @return max width in twip, or null.
     */
    public Integer getMaxWidth() {
        return maxWidth;
    }
}
