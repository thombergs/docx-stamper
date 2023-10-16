package org.wickedsource.docxstamper.replace;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.wickedsource.docxstamper.util.ParagraphWrapper;
import org.wickedsource.docxstamper.util.RunUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.wickedsource.docxstamper.util.ParagraphUtil.create;

@DisplayName("Utilities - Paragraph Wrapper")
class ParagraphWrapperTest {

	@Test
	void getTextReturnsAggregatedText() {
		ParagraphWrapper aggregator = loremIpsum();
		assertEquals("lorem ipsum", aggregator.getText());
	}

	private ParagraphWrapper loremIpsum() {
		return new ParagraphWrapper(create("lorem", " ", "ipsum"));
	}

	@Test
	void getRunsReturnsAddedRuns() {
		ParagraphWrapper aggregator = loremIpsum();
		assertEquals(3, aggregator.getRuns().size());
		assertEquals("lorem", RunUtil.getText(aggregator.getRuns().get(0)));
		assertEquals(" ", RunUtil.getText(aggregator.getRuns().get(1)));
		assertEquals("ipsum", RunUtil.getText(aggregator.getRuns().get(2)));
	}

	@Test
	void placeholderSpansFullSingleRun() {
		ParagraphWrapper wrapper = loremIpsum();
		wrapper.replace("lorem", RunUtil.create(""));
		assertEquals(" ipsum", wrapper.getText());
	}

	@Test
	void placeholderWithinSingleRun() {
		ParagraphWrapper wrapper = new ParagraphWrapper(create("My name is ${name}."));
		wrapper.replace("${name}", RunUtil.create("Bob"));
		assertEquals("My name is Bob.", wrapper.getText());
	}

	@Test
	void placeholderAtStartOfSingleRun() {
		ParagraphWrapper wrapper = new ParagraphWrapper(create("${name} my name is."));
		wrapper.replace("${name}", RunUtil.create("Yoda"));
		assertEquals("Yoda my name is.", wrapper.getText());
	}

	@Test
	void placeholderAtEndOfSingleRun() {
		ParagraphWrapper wrapper = new ParagraphWrapper(create("My name is ${name}"));
		wrapper.replace("${name}", RunUtil.create("Yoda"));
		assertEquals("My name is Yoda", wrapper.getText());
	}

	@Test
	void placeholderWithinMultipleRuns() {
		ParagraphWrapper wrapper = new ParagraphWrapper(create("My name is ${", "name", "}."));
		wrapper.replace("${name}", RunUtil.create("Yoda"));
		assertEquals("My name is Yoda.", wrapper.getText());
	}

	@Test
	void placeholderStartsWithinMultipleRuns() {
		ParagraphWrapper wrapper = new ParagraphWrapper(create("${", "name", "} my name is."));
		wrapper.replace("${name}", RunUtil.create("Yoda"));
		assertEquals("Yoda my name is.", wrapper.getText());
	}

	@Test
	void placeholderEndsWithinMultipleRuns() {
		ParagraphWrapper wrapper = new ParagraphWrapper(create("My name is ${", "name", "}"));
		wrapper.replace("${name}", RunUtil.create("Yoda"));
		assertEquals("My name is Yoda", wrapper.getText());
	}

	@Test
	void placeholderExactlySpansMultipleRuns() {
		ParagraphWrapper wrapper = new ParagraphWrapper(create("${", "name", "}"));
		wrapper.replace("${name}", RunUtil.create("Yoda"));
		assertEquals("Yoda", wrapper.getText());
	}
}
