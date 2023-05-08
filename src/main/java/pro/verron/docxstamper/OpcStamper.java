package org.wickedsource.docxstamper;

import org.docx4j.openpackaging.packages.OpcPackage;
import org.wickedsource.docxstamper.api.DocxStamperException;

import java.io.OutputStream;

public interface OpcStamper<T extends OpcPackage> {
	void stamp(T template, Object context, OutputStream outputStream) throws DocxStamperException;
}
