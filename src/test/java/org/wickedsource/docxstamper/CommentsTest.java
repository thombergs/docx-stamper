package org.wickedsource.docxstamper;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.Test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class CommentsTest {

    @Test
    public void test() throws IOException {
        InputStream in = getClass().getResourceAsStream("/template.docx");
        XWPFDocument document = new XWPFDocument(in);
        CommentStrippingXWPFDocumentWriter writer = new CommentStrippingXWPFDocumentWriter();
        writer.write(document, new FileOutputStream("D:\\no_comments.docx"));
    }
}
