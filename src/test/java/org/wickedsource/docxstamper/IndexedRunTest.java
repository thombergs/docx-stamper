package org.wickedsource.docxstamper;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.junit.Assert;
import org.junit.Test;

public class IndexedRunTest {

    @Test
    public void isNotTouchedByRangeBeforeStart() {
        IndexedRun run = new IndexedRun(5, 10, null);
        Assert.assertFalse(run.isTouchedByRange(0, 4));
    }

    @Test
    public void isNotTouchedByRangeAfterEnd() {
        IndexedRun run = new IndexedRun(5, 10, null);
        Assert.assertFalse(run.isTouchedByRange(11, 15));
    }

    @Test
    public void isTouchedByRangeEndingAtStart() {
        IndexedRun run = new IndexedRun(5, 10, null);
        Assert.assertTrue(run.isTouchedByRange(0, 5));
        Assert.assertTrue(run.isTouchedByRange(4, 5));
    }

    @Test
    public void isTouchedByRangeEndingAtEnd() {
        IndexedRun run = new IndexedRun(5, 10, null);
        Assert.assertTrue(run.isTouchedByRange(6, 10));
        Assert.assertTrue(run.isTouchedByRange(9, 10));
    }

    @Test
    public void isTouchedByRangeWithin() {
        IndexedRun run = new IndexedRun(5, 10, null);
        Assert.assertTrue(run.isTouchedByRange(5, 7));
    }

    @Test
    public void isTouchedByRangeBeforeStartAndAfterEnd() {
        IndexedRun run = new IndexedRun(5, 10, null);
        Assert.assertTrue(run.isTouchedByRange(4, 11));
        Assert.assertTrue(run.isTouchedByRange(0, 15));
    }

    @Test
    public void replaceWorksWithinRange() {
        IndexedRun run = new IndexedRun(5, 9, createRun("ipsum"));
        run.replace(5, 9, "lorem");
        Assert.assertEquals("lorem", run.getRun().getText(0));
        run.replace(8, 9, "el");
        Assert.assertEquals("lorel", run.getRun().getText(0));
        run.replace(8, 9, "em ipsum");
        Assert.assertEquals("lorem ipsum", run.getRun().getText(0));
    }

    private XWPFRun createRun(String text) {
        XWPFDocument d = new XWPFDocument();
        XWPFParagraph p = d.createParagraph();
        XWPFRun r = p.createRun();
        r.setText(text);
        return r;
    }
}