package org.wickedsource.docxstamper.replace;

import org.junit.Assert;
import org.junit.Test;
import org.wickedsource.docxstamper.util.ParagraphUtil;
import org.wickedsource.docxstamper.util.RunUtil;

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
        int replacementIndex = aggregator.cleanPlaceholder("lorem");
        Assert.assertEquals(0, replacementIndex);
        Assert.assertEquals(" ipsum", aggregator.getText());
    }

    @Test
    public void replaceFirstReplacesWithinSingleRun() {
        ParagraphWrapper aggregator = new ParagraphWrapper(ParagraphUtil.create("My name is ${name}."));
        int replacementIndex = aggregator.cleanPlaceholder("${name}");
        Assert.assertEquals(2, aggregator.getRuns().size());
        Assert.assertEquals(1, replacementIndex);
        Assert.assertEquals("My name is .", aggregator.getText());
    }

    @Test
    public void replaceFirstReplacesTwoRuns() {
        ParagraphWrapper aggregator = createLoremIpsumAggregator();
        int replacementIndex = aggregator.cleanPlaceholder("lorem ");
        Assert.assertEquals(0, replacementIndex);
        Assert.assertEquals("ipsum", aggregator.getText());
    }

    @Test
    public void replaceFirstReplacesThreeRuns() {
        ParagraphWrapper aggregator = createLoremIpsumAggregator();
        int replacementIndex = aggregator.cleanPlaceholder("lorem ipsum");
        Assert.assertEquals(0, replacementIndex);
        Assert.assertEquals("", aggregator.getText());
    }

    @Test
    public void replaceFirstReplacesOverlappingRuns() {
        ParagraphWrapper aggregator = createLoremIpsumAggregator();
        int replacementIndex = aggregator.cleanPlaceholder("lorem ips");
        Assert.assertEquals(0, replacementIndex);
        Assert.assertEquals("um", aggregator.getText());

    }

    private ParagraphWrapper createLoremIpsumAggregator() {
        ParagraphWrapper aggregator = new ParagraphWrapper(ParagraphUtil.create("lorem", " ", "ipsum"));
        return aggregator;
    }

}