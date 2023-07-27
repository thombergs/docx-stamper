package org.wickedsource.docxstamper.replace.typeresolver.image;

import lombok.Getter;
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
@Getter
public class Image {

    /**
     * -- GETTER --
     * <p>Getter for the field <code>imageBytes</code>.</p>
     *
     * @return an array of {@link byte} objects
     */
    private final byte[] imageBytes;

    /**
     * -- GETTER --
     * <p>Getter for the field <code>filename</code>.</p>
     *
     * @return a {@link String} object
     */
    private String filename;

    /**
     * -- GETTER --
     * Returns the expected alternative text to display for user that can't see the image itself.
     *
     * @return a {@link String} object
     */
    private String altText;

    /**
     * -- GETTER --
     * The expected max width for this image
     *
     * @return max width in twip, or null.
     */
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

}
