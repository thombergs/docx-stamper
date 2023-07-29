package org.wickedsource.docxstamper.replace;

import org.junit.jupiter.api.Test;
import org.wickedsource.docxstamper.util.IndexedRun;
import org.wickedsource.docxstamper.util.RunUtil;

import static org.junit.jupiter.api.Assertions.*;

class IndexedRunTest {

	@Test
	void isNotTouchedByRangeBeforeStart() {
		IndexedRun run = new IndexedRun(5, 10, 0, null);
		assertFalse(run.isTouchedByRange(0, 4));
	}

	@Test
	void isNotTouchedByRangeAfterEnd() {
		IndexedRun run = new IndexedRun(5, 10, 0, null);
		assertFalse(run.isTouchedByRange(11, 15));
	}

	@Test
	void isTouchedByRangeEndingAtStart() {
		IndexedRun run = new IndexedRun(5, 10, 0, null);
		assertTrue(run.isTouchedByRange(0, 5));
		assertTrue(run.isTouchedByRange(4, 5));
	}

	@Test
	void isTouchedByRangeEndingAtEnd() {
		IndexedRun run = new IndexedRun(5, 10, 0, null);
		assertTrue(run.isTouchedByRange(6, 10));
		assertTrue(run.isTouchedByRange(9, 10));
	}

	@Test
	void isTouchedByRangeWithin() {
		IndexedRun run = new IndexedRun(5, 10, 0, null);
		assertTrue(run.isTouchedByRange(5, 7));
	}

	@Test
	void isTouchedByRangeBeforeStartAndAfterEnd() {
		IndexedRun run = new IndexedRun(5, 10, 0, null);
		assertTrue(run.isTouchedByRange(4, 11));
		assertTrue(run.isTouchedByRange(0, 15));
	}

	@Test
	void replaceWorksWithinRange() {
		IndexedRun run = new IndexedRun(5, 9, 0, RunUtil.create("ipsum"));
		run.replace(5, 9, "lorem");
        assertEquals("lorem", RunUtil.getText(run.run()));
		run.replace(8, 9, "el");
        assertEquals("lorel", RunUtil.getText(run.run()));
		run.replace(8, 9, "em ipsum");
        assertEquals("lorem ipsum", RunUtil.getText(run.run()));
	}
}