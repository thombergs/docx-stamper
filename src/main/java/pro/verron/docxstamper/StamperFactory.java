package pro.verron.docxstamper;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.wickedsource.docxstamper.DocxStamper;
import org.wickedsource.docxstamper.DocxStamperConfiguration;
import org.wickedsource.docxstamper.preprocessor.MergeSameStyleRuns;
import org.wickedsource.docxstamper.preprocessor.RemoveProofErrors;

/**
 * Main class of the docx-stamper library.
 * <p>
 * This class can be used to create "stampers" that will open .docx templates
 * to create a .docx document filled with custom data at runtime.
 */
public class StamperFactory {

	/**
	 * Creates a new DocxStamper with the default configuration.
	 * Also adds the {@link RemoveProofErrors} and {@link MergeSameStyleRuns} preprocessors.
	 *
	 * @return a new DocxStamper
	 */
	public OpcStamper<WordprocessingMLPackage> newDocxStamper() {
		DocxStamperConfiguration configuration = new DocxStamperConfiguration();
		configuration.addPreprocessor(new RemoveProofErrors());
		configuration.addPreprocessor(new MergeSameStyleRuns());
		return new DocxStamper<>(configuration);
	}

	/**
	 * Creates a new DocxStamper with the default configuration.
	 * Does not add any preprocessors.
	 *
	 * @return a new DocxStamper
	 */
	public OpcStamper<WordprocessingMLPackage> nopreprocessingDocxStamper() {
		DocxStamperConfiguration configuration = new DocxStamperConfiguration();
		return new DocxStamper<>(configuration);
	}
}
