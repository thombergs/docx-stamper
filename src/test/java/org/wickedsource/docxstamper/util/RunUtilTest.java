package org.wickedsource.docxstamper.util;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pro.verron.docxstamper.utils.IOStreams;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Utilities - Docx Run Methods")
class RunUtilTest {

	@Test
	void getTextReturnsTextOfRun() throws Docx4JException {
		var document = loadDocument("singleRun.docx");
		var paragraph = (P) document.getMainDocumentPart().getContent().get(0);
		var run = (R) paragraph.getContent().get(0);
		assertEquals("This is the only run of text in this document.", RunUtil.getText(run));
	}

	/**
	 * <p>loadDocument.</p>
	 *
	 * @param resourceName a {@link java.lang.String} object
	 * @return a {@link org.docx4j.openpackaging.packages.WordprocessingMLPackage} object
	 * @throws org.docx4j.openpackaging.exceptions.Docx4JException if any.
	 */
	public WordprocessingMLPackage loadDocument(String resourceName) throws Docx4JException {
		var in = getClass().getResourceAsStream(resourceName);
		return WordprocessingMLPackage.load(in);
	}

	@Test
	void getTextReturnsValueDefinedBySetText() throws Docx4JException, IOException {
		var input = loadDocument("singleRun.docx");
		var paragraphIn = (P) input.getMainDocumentPart().getContent().get(0);
		var runIn = (R) paragraphIn.getContent().get(0);
		RunUtil.setText(runIn, "The text of this run was changed.");
		var out = IOStreams.getOutputStream();
		input.save(out);
		var in = IOStreams.getInputStream(out);
		var output = WordprocessingMLPackage.load(in);
		var paragraphOut = (P) output.getMainDocumentPart().getContent().get(0);
		var runOut = (R) paragraphOut.getContent().get(0);
		assertEquals("The text of this run was changed.", RunUtil.getText(runOut));
	}

}
