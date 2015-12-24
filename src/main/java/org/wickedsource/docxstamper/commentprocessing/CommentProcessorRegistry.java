package org.wickedsource.docxstamper.commentprocessing;

import org.apache.poi.xwpf.usermodel.*;
import org.wickedsource.docxstamper.walk.coordinates.ParagraphCoordinates;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Allows registration of ICommentProcessor objects. Each registered ICommentProcessor must implement an interface
 * which has to be specified at registration time. Provides several getter methods to access the registered ICommentProcessors.
 */
public class CommentProcessorRegistry implements ICommentProcessor {

    private Map<Class<?>, ICommentProcessor> commentProcessors = new HashMap<>();

    /**
     * Registers the specidied ICommentProcessor as an implementation of the specified interface.
     *
     * @param interfaceClass   the Interface which is implemented by the commentProcessor.
     * @param commentProcessor the commentProcessor implementing the specified interface.
     */
    public void registerCommentProcessor(Class<?> interfaceClass, ICommentProcessor commentProcessor) {
        this.commentProcessors.put(interfaceClass, commentProcessor);
    }

    /**
     * Returns the ICommentProcessor which is registered as an implementation of the specified interface.
     *
     * @param interfaceClass the interface for which to return an implementation,
     * @return the registered ICommentProcessor which is an implementation of the specified interface or null, if no such
     * ICommentProcessor is registered.
     */
    public ICommentProcessor getProcessorForInterface(Class<?> interfaceClass) {
        return commentProcessors.get(interfaceClass);
    }

    /**
     * Returns the set of interfaces for which an ICommentProcessor is registered.
     */
    public Set<Class<?>> getRegisteredInterfaces() {
        return commentProcessors.keySet();
    }

    /**
     * Returns the ICommentProcessor which implements the specified method.
     *
     * @param method the method for which to search an implementation.
     * @return the ICommentProcessor implementing the specified method or null if none exists.
     */
    public ICommentProcessor getProcessorForMethod(Method method) {
        for (Class<?> interfaceClass : commentProcessors.keySet()) {
            try {
                interfaceClass.getMethod(method.getName(), method.getParameterTypes());
                // no exception, so we found the method we're looking for
                return commentProcessors.get(interfaceClass);
            } catch (NoSuchMethodException e) {
                // continue search in the next interface class
            }
        }
        return null;
    }

    /**
     * Calls the method commitChanges on all registered ICommentProcessors.
     * @param document the document to be passed into the commit method of all ICommentProcessors.
     */
    public void commitChanges(XWPFDocument document){
        for(ICommentProcessor processor : commentProcessors.values()){
            processor.commitChanges(document);
        }
    }

    /**
     * Calls setCurrentParagraphCoordinates() on all registered ICommitProcessors.
     */
    public void setCurrentParagraphCoordinates(ParagraphCoordinates coordinates){
       for(ICommentProcessor processor : commentProcessors.values()){
           processor.setCurrentParagraphCoordinates(coordinates);
       }
    }

}
