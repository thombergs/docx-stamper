package org.wickedsource.docxstamper;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.springframework.expression.TypedValue;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.wickedsource.docxstamper.api.DocxStamperException;
import org.wickedsource.docxstamper.api.EvaluationContextConfigurer;
import org.wickedsource.docxstamper.api.preprocessor.PreProcessor;
import org.wickedsource.docxstamper.api.typeresolver.ITypeResolver;
import org.wickedsource.docxstamper.api.typeresolver.TypeResolverRegistry;
import org.wickedsource.docxstamper.el.ExpressionResolver;
import org.wickedsource.docxstamper.el.StandardMethodResolver;
import org.wickedsource.docxstamper.processor.CommentProcessorRegistry;
import org.wickedsource.docxstamper.replace.PlaceholderReplacer;
import org.wickedsource.docxstamper.replace.typeresolver.DateResolver;
import org.wickedsource.docxstamper.replace.typeresolver.LocalDateResolver;
import org.wickedsource.docxstamper.replace.typeresolver.LocalDateTimeResolver;
import org.wickedsource.docxstamper.replace.typeresolver.LocalTimeResolver;
import org.wickedsource.docxstamper.replace.typeresolver.image.Image;
import org.wickedsource.docxstamper.replace.typeresolver.image.ImageResolver;
import pro.verron.docxstamper.OpcStamper;
import pro.verron.docxstamper.StamperFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

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

	/**
	 * Creates a new DocxStamper with the default configuration.
	 *
	 * @deprecated since 1.6.4, use {@link StamperFactory#newDocxStamper()} or {@link StamperFactory#nopreprocessingDocxStamper()} instead.
	 */
	@Deprecated(since = "1.6.4", forRemoval = true)
	public DocxStamper() {
		this(new DocxStamperConfiguration());
	}

	/**
	 * Creates a new DocxStamper with the given configuration.
	 *
	 * @param configuration the configuration to use for this DocxStamper.
	 */
	public DocxStamper(DocxStamperConfiguration configuration) {
		this(
				configuration.isFailOnUnresolvedExpression(),
				configuration.isReplaceUnresolvedExpressions(),
				configuration.isLeaveEmptyOnExpressionError(),
				configuration.getUnresolvedExpressionsDefaultValue(),
				configuration.getLineBreakPlaceholder(),
				configuration.getEvaluationContextConfigurer(),
				configuration.getExpressionFunctions(),
				configureTypeResolverRegistry(configuration.getTypeResolvers(), configuration.getDefaultTypeResolver()),
				configuration.getCommentProcessors(),
				configuration.isReplaceNullValues(),
				configuration.getNullValuesDefault(),
				configuration.getPreprocessors(),
				configuration.getSpelParserConfiguration()
		);
	}

	private DocxStamper(
			boolean failOnUnresolvedExpression,
			boolean replaceUnresolvedExpressions,
			boolean leaveEmptyOnExpressionError,
			String unresolvedExpressionsDefaultValue,
			String lineBreakPlaceholder,
			EvaluationContextConfigurer evaluationContextConfigurer,
			Map<Class<?>, Object> expressionFunctions,
			TypeResolverRegistry typeResolverRegistry,
			Map<Class<?>, DocxStamperConfiguration.CommentProcessorFactory> configurationCommentProcessors,
			boolean replaceNullValues, String nullValuesDefault,
			List<PreProcessor> preprocessors,
			SpelParserConfiguration spelParserConfiguration
	) {
		var commentProcessors = new HashMap<Class<?>, Object>();

		Function<ReflectiveOperationException, TypedValue> onResolutionFail = failOnUnresolvedExpression
				? DocxStamper::throwException
				: exception -> new TypedValue(null);

		final StandardMethodResolver methodResolver = new StandardMethodResolver(
				commentProcessors,
				expressionFunctions,
				onResolutionFail);

		var evaluationContext = new StandardEvaluationContext();
		evaluationContextConfigurer.configureEvaluationContext(evaluationContext);
		evaluationContext.addMethodResolver(methodResolver);


		var expressionResolver = new ExpressionResolver(
				evaluationContext,
				spelParserConfiguration
		);

		var placeholderReplacer = new PlaceholderReplacer(
				typeResolverRegistry,
				expressionResolver,
				replaceNullValues,
				nullValuesDefault,
				failOnUnresolvedExpression,
				replaceUnresolvedExpressions,
				unresolvedExpressionsDefaultValue,
				leaveEmptyOnExpressionError,
				lineBreakPlaceholder
		);

		for (var entry : configurationCommentProcessors.entrySet()) {
			commentProcessors.put(entry.getKey(), entry.getValue().create(placeholderReplacer));
		}


		var commentProcessorRegistry = new CommentProcessorRegistry(
				placeholderReplacer,
				expressionResolver,
				commentProcessors,
				failOnUnresolvedExpression
		);

		this.placeholderReplacer = placeholderReplacer;
		this.commentProcessorRegistry = commentProcessorRegistry;
		this.preprocessors = preprocessors.stream().toList();
	}

	private static TypeResolverRegistry configureTypeResolverRegistry(
			Map<Class<?>, ITypeResolver<?>> typeResolvers,
			ITypeResolver<Object> defaultResolver
	) {
		var typeResolverRegistry = new TypeResolverRegistry(defaultResolver);
		typeResolverRegistry.registerTypeResolver(Image.class, new ImageResolver());
		typeResolverRegistry.registerTypeResolver(Date.class, new DateResolver());
		typeResolverRegistry.registerTypeResolver(LocalDate.class, new LocalDateResolver());
		typeResolverRegistry.registerTypeResolver(LocalDateTime.class, new LocalDateTimeResolver());
		typeResolverRegistry.registerTypeResolver(LocalTime.class, new LocalTimeResolver());

		for (var entry : typeResolvers.entrySet()) {
			//noinspection unchecked,rawtypes
			typeResolverRegistry.registerTypeResolver((Class) entry.getKey(), (ITypeResolver) entry.getValue());
		}
		return typeResolverRegistry;
	}

	private static TypedValue throwException(ReflectiveOperationException exception) {
		throw new DocxStamperException("Error calling method", exception);
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
