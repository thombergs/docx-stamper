package org.wickedsource.docxstamper;

import org.docx4j.TextUtils;
import org.docx4j.TraversalUtil;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wickedsource.docxstamper.util.ParagraphCollector;
import org.wickedsource.docxstamper.util.ThrowingSupplier;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Common methods to interact with docx documents.
 */
public abstract class AbstractDocx4jTest {

	public static final boolean KEEP_OUTPUT_FILE = Boolean.parseBoolean(System.getenv()
																			  .getOrDefault("keepOutputFile", "false"));
	private final Logger logger = LoggerFactory.getLogger(AbstractDocx4jTest.class);

	private final Map<OutputStream, Supplier<InputStream>> streams = new HashMap<>();

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

	protected OutputStream getOutputStream() throws IOException {
		if (KEEP_OUTPUT_FILE) {
			Path temporaryFile = Files.createTempFile(getClass().getSimpleName(), ".docx");
			logger.info("Saving DocxStamper output to temporary file %s".formatted(temporaryFile));
			OutputStream out = Files.newOutputStream(temporaryFile);
			ThrowingSupplier<InputStream> in = () -> Files.newInputStream(temporaryFile);
			streams.put(out, in);
			return out;
		} else {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			Supplier<InputStream> in = () -> new ByteArrayInputStream(out.toByteArray());
			streams.put(out, in);
			return out;
		}
	}

	protected InputStream getInputStream(OutputStream out) {
		return streams.get(out).get();
	}

	/**
	 * Stamps the given template resolving the expressions within the template against the specified contextRoot.
	 * Returns the resulting document after it has been saved and loaded again to ensure that changes in the Docx4j
	 * object structure were really transported into the XML of the .docx file.
	 */
	protected <T> WordprocessingMLPackage stampAndLoad(InputStream template, T contextRoot) throws IOException, Docx4JException {
		return stampAndLoad(template, contextRoot, new DocxStamperConfiguration().setFailOnUnresolvedExpression(false));
	}

	protected <T> WordprocessingMLPackage stampAndLoad(InputStream template, T contextRoot, DocxStamperConfiguration config) throws IOException, Docx4JException {
		OutputStream out = getOutputStream();
		DocxStamper<T> stamper = new DocxStamper<>(config);
		stamper.stamp(template, contextRoot, out);
		InputStream in = getInputStream(out);
		return WordprocessingMLPackage.load(in);
	}

	protected List<String> extractDocumentParagraphs(OutputStream out) throws Docx4JException {
		InputStream in = getInputStream(out);
		WordprocessingMLPackage document = WordprocessingMLPackage.load(in);
		ParagraphCollector visitor = new ParagraphCollector();
		TraversalUtil.visit(document.getMainDocumentPart().getContent(), visitor);
		return visitor.paragraphs().map(TextUtils::getText).toList();
	}
}
