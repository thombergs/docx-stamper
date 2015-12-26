package org.wickedsource.docxstamper.poi.commentprocessing.displayif;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.junit.Ignore;
import org.junit.Test;

import java.io.*;

public class DeleteFromTableTest {

    @Ignore
    @Test
    public void test() throws IOException {
        InputStream in = getClass().getResourceAsStream("tabletest.docx");
        XWPFDocument document = new XWPFDocument(in);
        XWPFTable table = document.getTables().get(0);
        table.getRow(2).getCell(0).removeParagraph(0);
        OutputStream out = new FileOutputStream(File.createTempFile("DeleteFromTableTest", ".docx"));
        document.write(out);
        out.close();
    }

}
