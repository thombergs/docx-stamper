package org.wickedsource.docxstamper.poi.commentprocessing;

import javassist.util.proxy.ProxyFactory;

import java.util.Set;

public class ContextFactory<T> {

    @SuppressWarnings("unchecked")
    public T createProxy(T contextRoot, CommentProcessorRegistry commentProcessors) throws Exception {
        CommentProcessorMethodHandler methodHandler = new CommentProcessorMethodHandler(contextRoot, commentProcessors);
        Set<Class<?>> registeredInterfaces = commentProcessors.getRegisteredInterfaces();
        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.setSuperclass(contextRoot.getClass());
        proxyFactory.setInterfaces(registeredInterfaces.toArray(new Class[registeredInterfaces.size()]));
        return (T) proxyFactory.create(new Class[0], new Object[0], methodHandler);
    }

}
