package org.wickedsource.docxstamper;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.wickedsource.docxstamper.api.DocxStamperException;
import org.wickedsource.docxstamper.api.preprocessor.PreProcessor;
import org.wickedsource.docxstamper.api.typeresolver.TypeResolverRegistry;
import org.wickedsource.docxstamper.el.ExpressionResolver;
import org.wickedsource.docxstamper.processor.CommentProcessorRegistry;
import org.wickedsource.docxstamper.replace.PlaceholderReplacer;
import org.wickedsource.docxstamper.replace.typeresolver.DateResolver;
import org.wickedsource.docxstamper.replace.typeresolver.FallbackResolver;
import org.wickedsource.docxstamper.replace.typeresolver.image.Image;
import org.wickedsource.docxstamper.replace.typeresolver.image.ImageResolver;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * <p>
 * Main class of the docx-stamper library. This class can be used to "stamp" .docx templates
 * to create a .docx document filled with custom data at runtime.
 * </p>
 */
public class DocxStamper<T> implements OpcStamper<WordprocessingMLPackage> {
	private final List<PreProcessor> preprocessors;
	private final PlaceholderReplacer placeholderReplacer;
	private final CommentProcessorRegistry commentProcessorRegistry;
	protected DocxStamperConfiguration configuration;

	/**
	 * @deprecated should use DocxStamper.createInstance
	 */
	@Deprecated(since = "1.6.4", forRemoval = true)
	public DocxStamper() {
		this(new DocxStamperConfiguration());
	}

	public DocxStamper(DocxStamperConfiguration configuration) {
		var failOnUnresolvedExpression = configuration.isFailOnUnresolvedExpression();
		var replaceUnresolvedExpressions = configuration.isReplaceUnresolvedExpressions();
		var leaveEmptyOnExpressionError = configuration.isLeaveEmptyOnExpressionError();

		var unresolvedExpressionsDefaultValue = configuration.getUnresolvedExpressionsDefaultValue();
		var lineBreakPlaceholder = configuration.getLineBreakPlaceholder();

		var evaluationContextConfigurer = configuration.getEvaluationContextConfigurer();

		var typeResolvers = configuration.getTypeResolvers();
		var expressionFunctions = configuration.getExpressionFunctions();

		var typeResolverRegistry = new TypeResolverRegistry(new FallbackResolver());
		typeResolverRegistry.registerTypeResolver(Image.class, new ImageResolver());
		typeResolverRegistry.registerTypeResolver(Date.class, new DateResolver());

		for (var entry : typeResolvers.entrySet()) {
			typeResolverRegistry.registerTypeResolver(entry.getKey(), entry.getValue());
		}

		var commentProcessors = new HashMap<Class<?>, Object>();

		var expressionResolver = new ExpressionResolver(
				failOnUnresolvedExpression,
				commentProcessors,
				expressionFunctions,
				evaluationContextConfigurer
		);

		var placeholderReplacer = new PlaceholderReplacer(
				typeResolverRegistry,
				expressionResolver,
				configuration.isReplaceNullValues(),
				configuration.getNullValuesDefault(),
				failOnUnresolvedExpression,
				replaceUnresolvedExpressions,
				unresolvedExpressionsDefaultValue,
				leaveEmptyOnExpressionError,
				lineBreakPlaceholder
		);

		for (var entry : configuration.getCommentProcessors().entrySet()) {
			commentProcessors.put(entry.getKey(), entry.getValue().create(placeholderReplacer));
		}

		var commentProcessorRegistry = new CommentProcessorRegistry(
				placeholderReplacer,
				expressionResolver,
				commentProcessors,
				failOnUnresolvedExpression
		);

		this.configuration = configuration;
		this.placeholderReplacer = placeholderReplacer;
		this.commentProcessorRegistry = commentProcessorRegistry;
		this.preprocessors = configuration.getPreprocessors().stream().toList();
	}

	/**
	 * <p>
	 * Reads in a .docx template and "stamps" it into the given OutputStream, using the specified context object to
	 * fill out any expressions it finds.
	 * </p>
	 * <p>
	 * In the .docx template you have the following options to influence the "stamping" process:
	 * </p>
	 * <ul>
	 * <li>Use expressions like ${name} or ${person.isOlderThan(18)} in the template's text. These expressions are resolved
	 * against the contextRoot object you pass into this method and are replaced by the results.</li>
	 * <li>Use comments within the .docx template to mark certain paragraphs to be manipulated. </li>
	 * </ul>
	 * <p>
	 * Within comments, you can put expressions in which you can use the following methods by default:
	 * </p>
	 * <ul>
	 * <li><em>displayParagraphIf(boolean)</em> to conditionally display paragraphs or not</li>
	 * <li><em>displayTableRowIf(boolean)</em> to conditionally display table rows or not</li>
	 * <li><em>displayTableIf(boolean)</em> to conditionally display whole tables or not</li>
	 * <li><em>repeatTableRow(List&lt;Object&gt;)</em> to create a new table row for each object in the list and resolve expressions
	 * within the table cells against one of the objects within the list.</li>
	 * </ul>
	 * <p>
	 * If you need a wider vocabulary of methods available in the comments, you can create your own ICommentProcessor
	 * and register it via getCommentProcessorRegistry().addCommentProcessor().
	 * </p>
	 *
	 * @param template    the .docx template.
	 * @param contextRoot the context root object against which all expressions found in the template are evaluated.
	 * @param out         the output stream in which to write the resulting .docx document.
	 * @throws DocxStamperException in case of an error.
	 */
	public void stamp(InputStream template, Object contextRoot, OutputStream out) throws DocxStamperException {
		try {
			WordprocessingMLPackage document = WordprocessingMLPackage.load(template);
			stamp(document, contextRoot, out);
		} catch (Docx4JException e) {
			throw new DocxStamperException(e);
		}
	}


	/**
	 * Same as stamp(InputStream, T, OutputStream) except that you may pass in a DOCX4J document as a template instead
	 * of an InputStream.
	 *
	 * @param document    the .docx template.
	 * @param contextRoot the context root object against which all expressions found in the template are evaluated.
	 * @param out         the output stream in which to write the resulting .docx document.
	 * @throws DocxStamperException in case of an error.
	 */
	@Override
	public void stamp(WordprocessingMLPackage document, Object contextRoot, OutputStream out) throws DocxStamperException {
		try {
			preprocess(document);
			processComments(document, contextRoot);
			replaceExpressions(document, contextRoot);
			document.save(out);
			commentProcessorRegistry.reset();
		} catch (Docx4JException e) {
			throw new DocxStamperException(e);
		}
	}

	private void preprocess(WordprocessingMLPackage document) {
		for (PreProcessor preprocessor : preprocessors) {
			preprocessor.process(document);
		}
	}

	private void processComments(final WordprocessingMLPackage document, Object contextObject) {
		commentProcessorRegistry.runProcessors(document, contextObject);
	}

	private void replaceExpressions(WordprocessingMLPackage document, Object contextObject) {
		placeholderReplacer.resolveExpressions(document, contextObject);
	}


}
