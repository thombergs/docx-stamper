package org.wickedsource.docxstamper.poi.commentprocessing.displayif;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.wickedsource.docxstamper.poi.DocxStamper;
import org.wickedsource.docxstamper.poi.RunAggregator;

import java.io.*;

@Ignore
public class DisplayIfProcessorTest {

    private XWPFDocument stamp(InputStream template) throws IOException {
        DisplayIfContext context = new DisplayIfContext();
        context.setSomeBooleanValue(false);
//        ByteArrayOutputStream out = new ByteArrayOutputStream();
        File tempFile = File.createTempFile("DisplayIfProcessorTest", ".docx");
        FileOutputStream out = new FileOutputStream(tempFile);
        DocxStamper<DisplayIfContext> stamper = new DocxStamper<>();
        stamper.stamp(template, context, out);
        out.close();
//        return new XWPFDocument(new ByteArrayInputStream(out.toByteArray()));
        return new XWPFDocument(new FileInputStream(tempFile));
    }

    @Test
    public void globalParagraphsAreRemoved() throws IOException {
        XWPFDocument document = stamp(getClass().getResourceAsStream("displayIf-globalParagraphs.docx"));

        Assert.assertEquals(6, document.getParagraphs().size());

        RunAggregator p1 = new RunAggregator(document.getParagraphArray(0));
        Assert.assertEquals("This paragraph is untouched.", p1.getText());

        RunAggregator p2 = new RunAggregator(document.getParagraphArray(1));
        Assert.assertEquals("", p2.getText());

        RunAggregator p3 = new RunAggregator(document.getParagraphArray(2));
        Assert.assertEquals("This paragraph should only be visible if someBooleanValue is false.", p3.getText());

        RunAggregator p4 = new RunAggregator(document.getParagraphArray(3));
        Assert.assertEquals("", p4.getText());
    }

    @Test
    @Ignore // POI seems to swallow whole tables instead of just the paragraphs within
    public void paragraphsWithinTablesAreRemoved() throws IOException {
        XWPFDocument document = stamp(getClass().getResourceAsStream("displayIf-paragraphsWithinTables.docx"));

        XWPFTable table = document.getTables().get(0);

        RunAggregator p1 = new RunAggregator(table.getRow(0).getCell(0).getParagraphs().get(0));
        Assert.assertEquals("This Paragraph should be untouched.", p1.getText());

        RunAggregator p2 = new RunAggregator(table.getRow(1).getCell(0).getParagraphs().get(0));
        Assert.assertEquals("This paragraph should only be displayed if someBooleanValue is false.", p2.getText());

        Assert.assertTrue(table.getRow(2).getCell(0).getParagraphs().isEmpty());

        RunAggregator p4 = new RunAggregator(table.getRow(4).getCell(0).getParagraphs().get(0));
        Assert.assertEquals("This Paragraph should be untouched.", p4.getText());

    }

    @Test
    public void paragraphsWithinNestedTablesAreRemoved() {
        Assert.fail();
    }

    @Test
    public void tablesAreRemoved() throws IOException {
        XWPFDocument document = stamp(getClass().getResourceAsStream("displayIf-removeTables.docx"));

        RunAggregator p1 = new RunAggregator((XWPFParagraph) document.getBodyElements().get(0));
        Assert.assertEquals("This paragraph is untouched.", p1.getText());

        XWPFTable table = (XWPFTable) document.getBodyElements().get(1);
        RunAggregator p2 = new RunAggregator(table.getRow(0).getCell(0).getParagraphs().get(0));
        Assert.assertEquals("This table is untouched.", p2.getText());

        RunAggregator p3 = new RunAggregator((XWPFParagraph) document.getBodyElements().get(2));
        Assert.assertEquals("This paragraph is untouched.", p3.getText());


    }

    @Test
    public void nestedTablesAreRemoved(){
        Assert.fail();
    }

    @Test
    public void tableRowsAreRemoved(){
        Assert.fail();
    }

    @Test
    public void tableRowsWithinNestedTablesAreRemoved(){
        Assert.fail();
    }

}