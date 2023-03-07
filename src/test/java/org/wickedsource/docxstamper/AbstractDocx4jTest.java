package org.wickedsource.docxstamper;

import org.docx4j.TextUtils;
import org.docx4j.TraversalUtil;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.docx4j.wml.RPr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wickedsource.docxstamper.util.ParagraphCollector;
import org.wickedsource.docxstamper.util.RunCollector;
import org.wickedsource.docxstamper.util.ThrowingSupplier;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.function.Supplier;

import static java.util.stream.Collectors.joining;

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

	protected List<List<String>> extractDocumentParagraphs(OutputStream out) throws Docx4JException {
		InputStream in = getInputStream(out);
		WordprocessingMLPackage document = WordprocessingMLPackage.load(in);
		ParagraphCollector visitor = new ParagraphCollector();
		TraversalUtil.visit(document.getMainDocumentPart().getContent(), visitor);
		return visitor.paragraphs()
					  .map(AbstractDocx4jTest::extractDocumentRuns)
					  .toList();
	}

	private static List<String> extractDocumentRuns(P p) {
		RunCollector runCollector = new RunCollector();
		TraversalUtil.visit(p, runCollector);
		return runCollector.runs()
						   .filter(o -> !o.getContent().isEmpty())
						   .filter(o -> !TextUtils.getText(o).isEmpty())
						   .map(o -> "" + TextUtils.getText(o) + serialize(o.getRPr()))
						   .toList();
	}

	private static String serialize(RPr rPr) {
		if (rPr == null) return "";
		SortedSet<String> set = new java.util.TreeSet<>();
		if (rPr.getRStyle() != null) set.add("rStyle=" + rPr.getRStyle().getVal());
		if (rPr.getB() != null) set.add("b=" + rPr.getB().isVal());
		if (rPr.getBdr() != null) set.add("bdr=xxx");
		if (rPr.getCaps() != null) set.add("caps=" + rPr.getCaps().isVal());
		if (rPr.getColor() != null) set.add("color=" + rPr.getColor().getVal());
		if (rPr.getRFonts() != null) set.add("rFonts=xxx:" + rPr.getRFonts().getHint().value());
		if (rPr.getI() != null) set.add("i=" + rPr.getI().isVal());
		if (rPr.getKern() != null) set.add("kern=" + rPr.getKern().getVal().intValue());
		if (rPr.getLang() != null) set.add("lang=" + rPr.getLang().getVal());
		if (rPr.getRPrChange() != null) set.add("rPrChange=xxx");
		if (rPr.getRStyle() != null) set.add("rStyle=" + rPr.getRStyle().getVal());
		if (rPr.getRtl() != null) set.add("rtl=" + rPr.getRtl().isVal());
		if (rPr.getShadow() != null) set.add("shadow=" + rPr.getShadow().isVal());
		if (rPr.getShd() != null) set.add("shd=" + rPr.getShd().getColor());
		if (rPr.getSmallCaps() != null) set.add("smallCaps=" + rPr.getSmallCaps().isVal());
		if (rPr.getVertAlign() != null) set.add("vertAlign=" + rPr.getVertAlign().getVal().value());

		return set.stream().collect(joining(", ", "(", ")"));
	}
}
