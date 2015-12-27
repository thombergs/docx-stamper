package org.wickedsource.docxstamper.proxy;

import javassist.util.proxy.ProxyFactory;
import org.wickedsource.docxstamper.docx4j.processor.ICommentProcessor;

public class ContextFactory<T> {

    /**
     * Creates a proxy object that is able to answer calls to all methods defined in the specified root object
     * as well as all methods defined in the specified interface class.
     *
     * @param root           the root object which is extended to support the specified interface.
     * @param interfaceClass the interface by which the root object is to be extended.
     * @param interfaceImpl  object implementing the specified interface. Calls on the interface are delegated to this object.
     * @return a proxy object implementing the interface of the specified root object as well as the specified interface class.
     * @throws Exception in case a proxy could not be created.
     */
    @SuppressWarnings("unchecked")
    public T createProxy(T root, Class<?> interfaceClass, ICommentProcessor interfaceImpl) throws Exception {
        ProxyMethodHandler methodHandler = new ProxyMethodHandler(root, interfaceClass, interfaceImpl);
        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.setSuperclass(root.getClass());
        proxyFactory.setInterfaces(new Class[]{interfaceClass});
        return (T) proxyFactory.create(new Class[0], new Object[0], methodHandler);
    }

}
