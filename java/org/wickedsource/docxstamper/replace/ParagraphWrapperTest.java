package org.wickedsource.docxstamper.replace;

import org.junit.Assert;
import org.junit.Test;
import org.wickedsource.docxstamper.util.ParagraphUtil;
import org.wickedsource.docxstamper.util.RunUtil;

import java.io.IOException;

public class ParagraphWrapperTest {

    @Test
    public void getTextReturnsAggregatedText() throws IOException {
        ParagraphWrapper aggregator = loremIpsum();
        Assert.assertEquals("lorem ipsum", aggregator.getText());
    }

    @Test
    public void getRunsReturnsAddedRuns() throws IOException {
        ParagraphWrapper aggregator = loremIpsum();
        Assert.assertEquals(3, aggregator.getRuns().size());
        Assert.assertEquals("lorem", RunUtil.getText(aggregator.getRuns().get(0)));
        Assert.assertEquals(" ", RunUtil.getText(aggregator.getRuns().get(1)));
        Assert.assertEquals("ipsum", RunUtil.getText(aggregator.getRuns().get(2)));
    }

    @Test
    public void placeholderSpansFullSingleRun() {
        ParagraphWrapper wrapper = loremIpsum();
        wrapper.replace("lorem", RunUtil.create(""));
        Assert.assertEquals(" ipsum", wrapper.getText());
    }

    @Test
    public void placeholderWithinSingleRun() {
        ParagraphWrapper wrapper = new ParagraphWrapper(ParagraphUtil.create("My name is ${name}."));
        wrapper.replace("${name}", RunUtil.create("Bob"));
        Assert.assertEquals("My name is Bob.", wrapper.getText());
    }

    @Test
    public void placeholderAtStartOfSingleRun() {
        ParagraphWrapper wrapper = new ParagraphWrapper(ParagraphUtil.create("${name} my name is."));
        wrapper.replace("${name}", RunUtil.create("Yoda"));
        Assert.assertEquals("Yoda my name is.", wrapper.getText());
    }

    @Test
    public void placeholderAtEndOfSingleRun() {
        ParagraphWrapper wrapper = new ParagraphWrapper(ParagraphUtil.create("My name is ${name}"));
        wrapper.replace("${name}", RunUtil.create("Yoda"));
        Assert.assertEquals("My name is Yoda", wrapper.getText());
    }

    @Test
    public void placeholderWithinMultipleRuns() {
        ParagraphWrapper wrapper = new ParagraphWrapper(ParagraphUtil.create("My name is ${", "name", "}."));
        wrapper.replace("${name}", RunUtil.create("Yoda"));
        Assert.assertEquals("My name is Yoda.", wrapper.getText());
    }

    @Test
    public void placeholderStartsWithinMultipleRuns() {
        ParagraphWrapper wrapper = new ParagraphWrapper(ParagraphUtil.create("${", "name", "} my name is."));
        wrapper.replace("${name}", RunUtil.create("Yoda"));
        Assert.assertEquals("Yoda my name is.", wrapper.getText());
    }

    @Test
    public void placeholderEndsWithinMultipleRuns() {
        ParagraphWrapper wrapper = new ParagraphWrapper(ParagraphUtil.create("My name is ${", "name", "}"));
        wrapper.replace("${name}", RunUtil.create("Yoda"));
        Assert.assertEquals("My name is Yoda", wrapper.getText());
    }

    @Test
    public void placeholderExactlySpansMultipleRuns() {
        ParagraphWrapper wrapper = new ParagraphWrapper(ParagraphUtil.create("${", "name", "}"));
        wrapper.replace("${name}", RunUtil.create("Yoda"));
        Assert.assertEquals("Yoda", wrapper.getText());
    }

    private ParagraphWrapper loremIpsum() {
        return new ParagraphWrapper(ParagraphUtil.create("lorem", " ", "ipsum"));
    }

}