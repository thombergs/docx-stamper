package pro.verron.docxstamper;

import org.docx4j.openpackaging.packages.OpcPackage;
import org.wickedsource.docxstamper.api.DocxStamperException;

import java.io.OutputStream;

/**
 * <p>OpcStamper interface.</p>
 *
 * @author joseph
 * @version $Id: $Id
 */
public interface OpcStamper<T extends OpcPackage> {
    /**
     * Stamps the template with the context and writes the result to the outputStream.
     *
     * @param template     template to stamp
     * @param context      context to use for stamping
     * @param outputStream output stream to write the result to
     * @throws org.wickedsource.docxstamper.api.DocxStamperException if the stamping fails
     */
	void stamp(T template, Object context, OutputStream outputStream) throws DocxStamperException;
}
