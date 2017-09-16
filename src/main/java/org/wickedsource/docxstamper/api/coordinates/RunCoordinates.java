package org.wickedsource.docxstamper.walk.coordinates;

import org.docx4j.wml.R;

public class RunCoordinates extends AbstractCoordinates {

	private R run;
	private int index;

	public RunCoordinates(R run, int index) {
		this.run = run;
		this.index = index;
	}

	public R getRun() {
		return run;
	}

	public int getIndex() {
		return index;
	}

	@Override
	public String toString() {
		String toString = String.format("run at index %d", index);
		return toString;
	}

}
