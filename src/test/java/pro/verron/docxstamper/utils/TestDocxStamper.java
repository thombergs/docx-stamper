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

/**
 * Common methods to interact with docx documents.
 */
public final class TestDocxStamper<T> {

	private final DocxStamper<T> stamper;
	private WordprocessingMLPackage document;

	public TestDocxStamper() {
		this(new DocxStamperConfiguration());
	}

	public TestDocxStamper(DocxStamperConfiguration config) {
		stamper = new DocxStamper<>(config);
	}

	/**
	 * Stamps the given template resolving the expressions within the template against the specified context.
	 * Returns the resulting document after it has been saved and loaded again to ensure that changes in the Docx4j
	 * object structure were really transported into the XML of the .docx file.
	 */
	public WordprocessingMLPackage stampAndLoad(InputStream template, T context) throws IOException, Docx4JException {
		OutputStream out = IOStreams.getOutputStream();
		stamper.stamp(template, context, out);
		InputStream in = IOStreams.getInputStream(out);
		return WordprocessingMLPackage.load(in);
	}

	public List<String> stampAndLoadAndExtract(InputStream template, T context) {
		Stringifier stringifier = new Stringifier(() -> document);
		return streamElements(template, context, P.class)
				.map(stringifier::stringify)
				.toList();
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

	public <C> List<String> stampAndLoadAndExtract(InputStream template, T context, Class<C> clazz) {
		Stringifier stringifier = new Stringifier(() -> document);
		return streamElements(template, context, clazz)
				.map(stringifier::extractDocumentRuns)
				.toList();
	}
}
