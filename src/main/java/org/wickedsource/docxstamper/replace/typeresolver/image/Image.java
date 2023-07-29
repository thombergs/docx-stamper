package org.wickedsource.docxstamper.replace.typeresolver.image;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This class describes an image which will be inserted into document.
 *
 * @author joseph
 * @version $Id: $Id
 */
public class Image {

    private final byte[] imageBytes;
    private Integer maxWidth;

    /**
     * <p>Constructor for Image.</p>
     *
     * @param in - content of the image as InputStream
     * @throws java.io.IOException if any.
     */
    public Image(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        IOUtils.copy(in, out);
        this.imageBytes = out.toByteArray();
    }

    /**
     * <p>Constructor for Image.</p>
     *
     * @param in - content of the image as InputStream
     * @param maxWidth - max width of the image in twip
     * @throws java.io.IOException if any.
     */
    public Image(InputStream in, Integer maxWidth) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        IOUtils.copy(in, out);
        this.imageBytes = out.toByteArray();
        this.maxWidth = maxWidth;
    }

    /**
     * <p>Constructor for Image.</p>
     *
     * @param imageBytes - content of the image as array of the bytes
     */
    public Image(byte[] imageBytes) {
        this.imageBytes = imageBytes;
    }

    /**
     * <p>Constructor for Image.</p>
     *
     * @param imageBytes - content of the image as array of the bytes
     * @param maxWidth - max width of the image in twip
     */
    public Image(byte[] imageBytes, Integer maxWidth) {
        this.imageBytes = imageBytes;
        this.maxWidth = maxWidth;
    }

    /**
     * <p>Getter for the field <code>maxWidth</code>.</p>
     *
     * @return a {@link java.lang.Integer} object
     */
    public Integer getMaxWidth() {
        return maxWidth;
    }

    /**
     * <p>Getter for the field <code>imageBytes</code>.</p>
     *
     * @return an array of {@link byte} objects
     */
    public byte[] getImageBytes() {
        return imageBytes;
    }
}
