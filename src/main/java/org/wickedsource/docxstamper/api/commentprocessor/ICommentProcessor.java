package org.wickedsource.docxstamper.api.commentprocessor;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.wickedsource.docxstamper.walk.coordinates.ParagraphCoordinates;

/**
 * <p>
 * In a .docx template used by DocxStamper, you can comment paragraphs of text to manipulate them. The comments in
 * the .docx template are passed to an implementation of ICommentProcessor that understands the expression used
 * within the comment. Thus, you can implement your own ICommentProcessor to extend the expression language available
 * in comments in the .docx template.
 * </p>
 * <p>
 * To implement a comment processor, you have to do the following steps:
 * <ol>
 * <li>Create an interface that defines the custom method(s) you want to expose to the expression language used in .docx comments</li>
 * <li>Create an implementation of your interface</li>
 * <li>Register your comment processor with DocxStamper</li>
 * </ol>
 * </p>
 * <p><strong>1. Creating a comment processor interface</strong><br/>
 * For example, if you want to create a comment processor that
 * makes a paragraph of text bold based on some condition, you would create an interface with the method
 * boldIf(boolean condition).
 * </p>
 * <p><strong>2. Creating an implementation of your interface</strong><br/>
 * Your implementation class must also implement the Interface
 * ICommentProcessor. To stay in the above example, when the boldIf method is called, simply keep track of the paragraphs that are to be made bold.
 * The currently processed paragraph is passed into the method setCurrentParagraphCoordinates() before your own method
 * (in this case boldIf()) is called.
 * Within the method commitChanges() you then do the manipulations on the word document, i.e. make the paragraphs
 * that were commented bold.
 * </p>
 * <p><strong>3. Registering you comment processor with DocxStamper</strong><br/>
 * Register your comment processor in DocxStamper by calling DocxStamper#getCommentProcessorRegistry().registerCommentProcessor().
 * </p>
 */
public interface ICommentProcessor {

    /**
     * This method is called after all comments in the .docx template have been passed to the comment processor.
     * All manipulations of the .docx document SHOULD BE done in this method. If certain manipulations are already done
     * within in the custom methods of a comment processor, the ongoing iteration over the paragraphs in the document
     * may be disturbed.
     *
     * @param document The word document that can be manipulated by using the DOCX4J api.
     */
    public void commitChanges(WordprocessingMLPackage document);

    /**
     * Passes the paragraph that is currently being processed (i.e. the paragraph that is commented in the
     * .docx template. This method is always called BEFORE the custom methods of the custom comment processor interface
     * are called.
     *
     * @param coordinates coordinates of the currently processed paragraph within the template.
     */
    public void setCurrentParagraphCoordinates(ParagraphCoordinates coordinates);

}
