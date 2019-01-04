package org.wickedsource.docxstamper;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * Common methods to interact with docx documents.
 */
public abstract class AbstractDocx4jTest {

    private Logger logger = LoggerFactory.getLogger(AbstractDocx4jTest.class);

    private File tempFile;

    protected WordprocessingMLPackage loadDocument(String resourceName) throws Docx4JException {
        InputStream in = getClass().getResourceAsStream(resourceName);
        return WordprocessingMLPackage.load(in);
    }

    /**
     * Saves the given document into a temporal ByteArrayOutputStream and loads it from there again. This is useful to
     * check if changes in the Docx4j object structure are really transported into the XML of the .docx file.
     *
     * @param document the document to save and load again.
     * @return the document after it has been saved and loaded again.
     */
    protected WordprocessingMLPackage saveAndLoadDocument(WordprocessingMLPackage document) throws Docx4JException, IOException {
        OutputStream out = getOutputStream();
        document.save(out);
        InputStream in = getInputStream(out);
        return WordprocessingMLPackage.load(in);
    }

    /**
     * Stamps the given template resolving the expressions within the template against the specified contextRoot.
     * Returns the resulting document after it has been saved and loaded again to ensure that changes in the Docx4j
     * object structure were really transported into the XML of the .docx file.
     */
    protected <T> WordprocessingMLPackage stampAndLoad(InputStream template, T contextRoot) throws IOException, Docx4JException {
        return stampAndLoad(template, contextRoot, createConfiguration());
    }

    protected DocxStamperConfiguration createConfiguration() {
        return new DocxStamperConfiguration();
    }

    protected <T> WordprocessingMLPackage stampAndLoad(InputStream template, T contextRoot, DocxStamperConfiguration config) throws IOException, Docx4JException {
        OutputStream out = getOutputStream();
        DocxStamper stamper = new DocxStamper(config);
        stamper.stamp(template, contextRoot, out);
        InputStream in = getInputStream(out);
        return WordprocessingMLPackage.load(in);
    }

    protected OutputStream getOutputStream() throws IOException {
        OutputStream out;
        if (Boolean.valueOf(System.getProperty("keepOutputFile"))) {
            tempFile = File.createTempFile(getClass().getSimpleName(), ".docx");
            logger.info(String.format(">>>>>>>> Saving DocxStamper output to temporary file %s <<<<<<<<", tempFile.getAbsolutePath()));
            out = new FileOutputStream(tempFile);
        } else {
            out = new ByteArrayOutputStream();
        }
        return out;
    }

    protected InputStream getInputStream(OutputStream out) throws FileNotFoundException {
        InputStream in;
        if (Boolean.valueOf(System.getProperty("keepOutputFile"))) {
            in = new FileInputStream(tempFile);

        } else {
            in = new ByteArrayInputStream(((ByteArrayOutputStream) out).toByteArray());
        }
        return in;
    }


}
