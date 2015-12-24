package org.wickedsource.docxstamper;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class RunAggregatorTest {

    @Test
    public void getTextReturnsAggregatedText() throws IOException {
        RunAggregator aggregator = createLoremIpsumAggregator();
        Assert.assertEquals("lorem ipsum", aggregator.getText());
    }

    @Test
    public void getRunsReturnsAddedRuns() throws IOException {
        RunAggregator aggregator = createLoremIpsumAggregator();
        Assert.assertEquals(3, aggregator.getRuns().size());
        Assert.assertEquals("lorem", aggregator.getRuns().get(0).getText(0));
        Assert.assertEquals(" ", aggregator.getRuns().get(1).getText(0));
        Assert.assertEquals("ipsum", aggregator.getRuns().get(2).getText(0));
    }

    @Test
    public void replaceFirstReplacesSingleRun() {
        RunAggregator aggregator = createLoremIpsumAggregator();
        aggregator.replaceFirst("lorem", "ipsum");
        Assert.assertEquals("ipsum ipsum", aggregator.getText());
    }

    @Test
    public void replaceFirstReplacesWithinSingleRun(){
        RunAggregator aggregator = new RunAggregator();

        XWPFDocument doc = new XWPFDocument();
        XWPFParagraph p = doc.createParagraph();

        XWPFRun run = p.createRun();
        run.setText("My name is ${name}.");
        aggregator.addRun(run);

        aggregator.replaceFirst("${name}", "Bob");

        Assert.assertEquals("My name is Bob.", aggregator.getText());
    }

    @Test
    public void replaceFirstReplacesTwoRuns() {
        RunAggregator aggregator = createLoremIpsumAggregator();
        aggregator.replaceFirst("lorem ", "ipsum");
        Assert.assertEquals("ipsumipsum", aggregator.getText());
    }

    @Test
    public void replaceFirstReplacesThreeRuns() {
        RunAggregator aggregator = createLoremIpsumAggregator();
        aggregator.replaceFirst("lorem ipsum", "ipsum");
        Assert.assertEquals("ipsum", aggregator.getText());
    }

    @Test
    public void replaceFirstReplacesOverlappingRuns() {
        RunAggregator aggregator = createLoremIpsumAggregator();
        aggregator.replaceFirst("lorem ips", "ipsum");
        Assert.assertEquals("ipsumum", aggregator.getText());
    }

    private RunAggregator createLoremIpsumAggregator() {
        RunAggregator aggregator = new RunAggregator();

        XWPFDocument doc = new XWPFDocument();
        XWPFParagraph p = doc.createParagraph();

        XWPFRun run = p.createRun();
        run.setText("lorem");
        aggregator.addRun(run);

        XWPFRun run2 = p.createRun();
        run2.setText(" ");
        aggregator.addRun(run2);

        XWPFRun run3 = p.createRun();
        run3.setText("ipsum");
        aggregator.addRun(run3);

        return aggregator;
    }

}