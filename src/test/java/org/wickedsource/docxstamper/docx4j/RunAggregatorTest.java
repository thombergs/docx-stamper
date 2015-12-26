package org.wickedsource.docxstamper.docx4j;

import org.junit.Assert;
import org.junit.Test;
import org.wickedsource.docxstamper.docx4j.util.RunUtil;

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
        Assert.assertEquals("lorem", RunUtil.getText(aggregator.getRuns().get(0)));
        Assert.assertEquals(" ", RunUtil.getText(aggregator.getRuns().get(1)));
        Assert.assertEquals("ipsum", RunUtil.getText(aggregator.getRuns().get(2)));
    }

    @Test
    public void replaceFirstReplacesSingleRun() {
        RunAggregator aggregator = createLoremIpsumAggregator();
        aggregator.replaceFirst("lorem", "ipsum");
        Assert.assertEquals("ipsum ipsum", aggregator.getText());
    }

    @Test
    public void replaceFirstReplacesWithinSingleRun() {
        RunAggregator aggregator = new RunAggregator();
        aggregator.addRun(RunUtil.create("My name is ${name}."));
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
        aggregator.addRun(RunUtil.create("lorem"));
        aggregator.addRun(RunUtil.create(" "));
        aggregator.addRun(RunUtil.create("ipsum"));
        return aggregator;
    }

}