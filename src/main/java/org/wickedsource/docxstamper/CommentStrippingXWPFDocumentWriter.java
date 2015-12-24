package org.wickedsource.docxstamper;

import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class CommentStrippingXWPFDocumentWriter {

    /**
     * Writes the specified XWPFDocument to the specified OutputStream, stripping the comments on the way. Warning:
     * the XWPFDocument object is fully written into a bytearray buffer so that the document will be doubly in memory for
     * a time (once in the XWPFDocument object and once in the bytearray).
     *
     * @param document the document to write into an OutputStream without comments.
     * @param out      the OutputStream to write the document into.
     * @throws IOException
     */
    public void write(XWPFDocument document, OutputStream out) throws IOException {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        document.write(byteOut);

        ZipInputStream zipIn = new ZipInputStream(new ByteArrayInputStream(byteOut.toByteArray()));
        ZipOutputStream zipOut = new ZipOutputStream(out);
        ZipEntry zipEntry;
        while ((zipEntry = zipIn.getNextEntry()) != null) {
            if (!"word/comments.xml".equals(zipEntry.getName())) {
                zipOut.putNextEntry(zipEntry);
                int b;
                while ((b = zipIn.read()) != -1) {
                    zipOut.write(b);
                }
            }
        }
        zipOut.close();
    }
}
