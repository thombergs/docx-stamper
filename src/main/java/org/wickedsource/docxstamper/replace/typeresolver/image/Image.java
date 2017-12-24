package org.wickedsource.docxstamper.replace.typeresolver.image;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Image {

    private byte[] imageBytes;

    private String filename;

    private String altText;

    private Integer maxWidth;

    public Image(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        IOUtils.copy(in, out);
        this.imageBytes = out.toByteArray();
    }

    public Image(InputStream in, Integer maxWidth) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        IOUtils.copy(in, out);
        this.imageBytes = out.toByteArray();
        this.maxWidth = maxWidth;
    }

    public Image(byte[] imageBytes) {
        this.imageBytes = imageBytes;
    }

    public Image(byte[] imageBytes, Integer maxWidth) {
        this.imageBytes = imageBytes;
        this.maxWidth = maxWidth;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getAltText() {
        return altText;
    }

    public void setAltText(String altText) {
        this.altText = altText;
    }

    public byte[] getImageBytes() {
        return imageBytes;
    }

    public void setImageBytes(byte[] imageBytes) {
        this.imageBytes = imageBytes;
    }

    public Integer getMaxWidth() {
        return maxWidth;
    }

    public void setMaxWidth(Integer maxWidth) {
        this.maxWidth = maxWidth;
    }
}
