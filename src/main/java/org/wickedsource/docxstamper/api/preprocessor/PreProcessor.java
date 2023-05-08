package org.wickedsource.docxstamper.api.preprocessor;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

public interface PreProcessor {
	void process(WordprocessingMLPackage document);
}
