package pro.verron.docxstamper.utils;

import org.docx4j.TraversalUtil;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.wickedsource.docxstamper.DocxStamper;
import org.wickedsource.docxstamper.DocxStamperConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

/**
 * Common methods to interact with docx documents.
 *
 * @author joseph
 * @version $Id: $Id
 * @since 1.6.5
 */
public final class TestDocxStamper<T> {

	private final DocxStamper<T> stamper;
	private WordprocessingMLPackage document;

	/**
	 * <p>Constructor for TestDocxStamper.</p>
	 *
	 * @param config a {@link org.wickedsource.docxstamper.DocxStamperConfiguration} object
	 */
	public TestDocxStamper(DocxStamperConfiguration config) {
		stamper = new DocxStamper<>(config);
	}

	/**
	 * Stamps the given template resolving the expressions within the template against the specified context.
	 * Returns the resulting document after it has been saved and loaded again to ensure that changes in the Docx4j
	 * object structure were really transported into the XML of the .docx file.
	 *
	 * @param template a {@link java.io.InputStream} object
	 * @param context a T object
	 * @return a {@link org.docx4j.openpackaging.packages.WordprocessingMLPackage} object
	 * @throws java.io.IOException if any.
	 * @throws org.docx4j.openpackaging.exceptions.Docx4JException if any.
	 */
	public WordprocessingMLPackage stampAndLoad(InputStream template, T context) throws IOException, Docx4JException {
		OutputStream out = IOStreams.getOutputStream();
		stamper.stamp(template, context, out);
		InputStream in = IOStreams.getInputStream(out);
		return WordprocessingMLPackage.load(in);
	}

	/**
	 * <p>stampAndLoadAndExtract.</p>
	 *
	 * @param template a {@link java.io.InputStream} object
	 * @param context a T object
	 * @return a {@link java.util.List} object
	 */
	public String stampAndLoadAndExtract(InputStream template, T context) {
		Stringifier stringifier = new Stringifier(() -> document);
		return streamElements(template, context, P.class)
				.map(stringifier::stringify)
				.collect(joining("\n"));
	}

	private <C> Stream<C> streamElements(InputStream template, T context, Class<C> clazz) {
		Stream<C> elements;
		try {
			var out = IOStreams.getOutputStream();
			stamper.stamp(template, context, out);
			var in = IOStreams.getInputStream(out);
			document = WordprocessingMLPackage.load(in);
			var visitor = newCollector(clazz);
			var mainDocumentPart = document.getMainDocumentPart();
			var content = mainDocumentPart.getContent();
			TraversalUtil.visit(content, visitor);
			elements = visitor.elements();
		} catch (Docx4JException | IOException e) {
			throw new RuntimeException(e);
		}
		return elements;
	}

	private <C> DocxCollector<C> newCollector(Class<C> type) {
		return new DocxCollector<>(type);
	}

	/**
	 * <p>stampAndLoadAndExtract.</p>
	 *
	 * @param template a {@link java.io.InputStream} object
	 * @param context a T object
	 * @param clazz a {@link java.lang.Class} object
	 * @param <C> a C class
	 * @return a {@link java.util.List} object
	 */
	public <C> List<String> stampAndLoadAndExtract(InputStream template, T context, Class<C> clazz) {
		Stringifier stringifier = new Stringifier(() -> document);
		return streamElements(template, context, clazz)
				.map(stringifier::extractDocumentRuns)
				.toList();
	}
}
