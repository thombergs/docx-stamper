package io.reflectoring.docxstamper.api.typeresolver;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

/**
 * <p>
 * A type resolver is responsible for mapping an object of a certain Java class to an object of the DOCX4J api that
 * can be put into the .docx document. Type resolvers are used to replace placeholders within the .docx template.
 * </p>
 * <p>
 * Example: if an expression returns a Date object as result, this date object is passed to a DateResolver which
 * creates a org.docx4j.wml.R object (run of text) containing the properly formatted date string.
 * </p>
 * <p>
 * To use your own type resolver, implement this interface and register your implementation by calling
 * DocxStamper.getTypeResolverRegistry().addTypeResolver().
 * </p>
 */
public interface ITypeResolver<S, T> {

    /**
     * This method is called when a placeholder in the .docx template is to replaced by the result of an expression that
     * was found in the .docx template. It creates an object of the DOCX4J api that is put in the place of the found
     * expression.
     *
     * @param document         the word document that can be accessed via the DOCX4J api.
     * @param expressionResult the result of an expression. Only objects of classes this type resolver is registered for
     *                         within the TypeResolverRegistrey are passed into this method.
     * @return an object of the DOCX4J api (usually of type org.docx4j.wml.R = "run of text") that will be put in the place of an
     * expression found in the .docx document.
     */
    T resolve(WordprocessingMLPackage document, S expressionResult);

}
