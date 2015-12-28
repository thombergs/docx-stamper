package org.wickedsource.docxstamper.docx4j.util;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class ParagraphWrapperTest {

    @Test
    public void getTextReturnsAggregatedText() throws IOException {
        ParagraphWrapper aggregator = createLoremIpsumAggregator();
        Assert.assertEquals("lorem ipsum", aggregator.getText());
    }

    @Test
    public void getRunsReturnsAddedRuns() throws IOException {
        ParagraphWrapper aggregator = createLoremIpsumAggregator();
        Assert.assertEquals(3, aggregator.getRuns().size());
        Assert.assertEquals("lorem", RunUtil.getText(aggregator.getRuns().get(0)));
        Assert.assertEquals(" ", RunUtil.getText(aggregator.getRuns().get(1)));
        Assert.assertEquals("ipsum", RunUtil.getText(aggregator.getRuns().get(2)));
    }

    @Test
    public void replaceFirstReplacesSingleRun() {
        ParagraphWrapper aggregator = createLoremIpsumAggregator();
        aggregator.replaceFirst("lorem", "ipsum");
        Assert.assertEquals("ipsum ipsum", aggregator.getText());
    }

    @Test
    public void replaceFirstReplacesWithinSingleRun() {
        ParagraphWrapper aggregator = new ParagraphWrapper(ParagraphUtil.create("My name is ${name}."));
        aggregator.replaceFirst("${name}", "Bob");
        Assert.assertEquals("My name is Bob.", aggregator.getText());
    }

    @Test
    public void replaceFirstReplacesTwoRuns() {
        ParagraphWrapper aggregator = createLoremIpsumAggregator();
        aggregator.replaceFirst("lorem ", "ipsum");
        Assert.assertEquals("ipsumipsum", aggregator.getText());
    }

    @Test
    public void replaceFirstReplacesThreeRuns() {
        ParagraphWrapper aggregator = createLoremIpsumAggregator();
        aggregator.replaceFirst("lorem ipsum", "ipsum");
        Assert.assertEquals("ipsum", aggregator.getText());
    }

    @Test
    public void replaceFirstReplacesOverlappingRuns() {
        ParagraphWrapper aggregator = createLoremIpsumAggregator();
        aggregator.replaceFirst("lorem ips", "ipsum");
        Assert.assertEquals("ipsumum", aggregator.getText());
    }

    private ParagraphWrapper createLoremIpsumAggregator() {
        ParagraphWrapper aggregator = new ParagraphWrapper(ParagraphUtil.create("lorem", " ", "ipsum"));
        return aggregator;
    }

}