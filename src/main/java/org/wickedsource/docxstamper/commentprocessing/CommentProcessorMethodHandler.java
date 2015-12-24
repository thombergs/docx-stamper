package org.wickedsource.docxstamper.commentprocessing;

import javassist.util.proxy.MethodHandler;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import java.lang.reflect.Method;

public class CommentProcessorMethodHandler implements MethodHandler {

    private final Object contextRoot;

    private CommentProcessorRegistry commentProcessors;

    /**
     * Constructs a MethodHandler that serves as a Proxy for method calls on the specified contextRoot object and all
     * interfaces registered in the given CommentProcessorRegistry. When a method is called on this proxy object
     * the method signature is checked and the method call is delegated to the first CommentProcessor who satisfies
     * the method signature. If no CommentProcessor is found, the method call is delegated to the specified contextRoot
     * object. If that object cannot satisfy the method signature, an Exception is thrown..
     *
     * @param contextRoot       the contextRoot whose methods are "merged" with all methods of the registered CommentProcessors.
     * @param commentProcessors registry containing a set of registered CommentProcessors.
     * @throws Exception
     */
    public CommentProcessorMethodHandler(Object contextRoot, CommentProcessorRegistry commentProcessors) throws Exception {
        this.contextRoot = contextRoot;
        this.commentProcessors = commentProcessors;
    }

    @Override
    public Object invoke(Object o, Method method, Method method2, Object[] args) throws Throwable {
        ICommentProcessor commentProcessor = commentProcessors.getProcessorForMethod(method);
        if (commentProcessor != null) {
            return method.invoke(commentProcessor, args);
        } else {
            return method.invoke(contextRoot, args);
        }
    }

}
