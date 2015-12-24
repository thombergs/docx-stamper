package org.wickedsource.docxstamper.commentprocessing;

import org.wickedsource.docxstamper.commentprocessing.displayif.IDisplayIfProcessor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class CommentProcessingProxy<T> implements InvocationHandler {

    private T contextObject;
    private Class<T> contextInterface;

    public CommentProcessingProxy(T contextObject, Class<T> contextInterface) {
        this.contextObject = contextObject;
        this.contextInterface = contextInterface;
    }


    public void registerCommentProcessor(ICommentProcessor processor) {

    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if ("displayIf".equals(method.getName())) {
            System.out.println("displayIf() was called with argument " + args[0]);
        } else {
            return method.invoke(contextObject, args);
        }

        return null;
    }

    public static <T> Object newInstance(T contextObject, Class<T> contextInterface) {
        return Proxy.newProxyInstance(contextObject.getClass().getClassLoader(), new Class[]{contextInterface, IDisplayIfProcessor.class}, new CommentProcessingProxy<>(contextObject, contextInterface));
    }

}
