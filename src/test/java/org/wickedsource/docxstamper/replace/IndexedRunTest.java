package org.wickedsource.docxstamper.replace;

import org.docx4j.jaxb.Context;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.R;
import org.junit.Assert;
import org.junit.Test;
import org.wickedsource.docxstamper.util.RunUtil;

public class IndexedRunTest {

    private final ObjectFactory factory = Context.getWmlObjectFactory();

    @Test
    public void isNotTouchedByRangeBeforeStart() {
        IndexedRun run = new IndexedRun(5, 10, 0, null);
        Assert.assertFalse(run.isTouchedByRange(0, 4));
    }

    @Test
    public void isNotTouchedByRangeAfterEnd() {
        IndexedRun run = new IndexedRun(5, 10, 0, null);
        Assert.assertFalse(run.isTouchedByRange(11, 15));
    }

    @Test
    public void isTouchedByRangeEndingAtStart() {
        IndexedRun run = new IndexedRun(5, 10, 0, null);
        Assert.assertTrue(run.isTouchedByRange(0, 5));
        Assert.assertTrue(run.isTouchedByRange(4, 5));
    }

    @Test
    public void isTouchedByRangeEndingAtEnd() {
        IndexedRun run = new IndexedRun(5, 10, 0, null);
        Assert.assertTrue(run.isTouchedByRange(6, 10));
        Assert.assertTrue(run.isTouchedByRange(9, 10));
    }

    @Test
    public void isTouchedByRangeWithin() {
        IndexedRun run = new IndexedRun(5, 10, 0, null);
        Assert.assertTrue(run.isTouchedByRange(5, 7));
    }

    @Test
    public void isTouchedByRangeBeforeStartAndAfterEnd() {
        IndexedRun run = new IndexedRun(5, 10, 0, null);
        Assert.assertTrue(run.isTouchedByRange(4, 11));
        Assert.assertTrue(run.isTouchedByRange(0, 15));
    }

    @Test
    public void replaceWorksWithinRange() {
        IndexedRun run = new IndexedRun(5, 9, 0, createRun("ipsum"));
        run.replace(5, 9, "lorem");
        Assert.assertEquals("lorem", RunUtil.getText(run.getRun()));
        run.replace(8, 9, "el");
        Assert.assertEquals("lorel", RunUtil.getText(run.getRun()));
        run.replace(8, 9, "em ipsum");
        Assert.assertEquals("lorem ipsum", RunUtil.getText(run.getRun()));
    }

    private R createRun(String text) {
        R run = factory.createR();
        RunUtil.setText(run, text);
        return run;
    }
}