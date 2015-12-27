package org.wickedsource.docxstamper.proxy;

import javassist.util.proxy.MethodHandler;

import java.lang.reflect.Method;

public class ProxyMethodHandler implements MethodHandler {

    private final Object contextRoot;

    private Class<?> interfaceClass;

    private Object interfaceImpl;

    /**
     * Constructs a MethodHandler that serves as a Proxy for method calls on the specified root object and a
     * specified interface. The invoke-method delegates method calls to the specified interfaceImpl object if
     * the method is supported by it's interface. If not, the method call is delegated to the root object. If the
     * root object does not support the method call, an exception is thrown.
     *
     * @param root           the root object whose methods are to be supported by this proxy.
     * @param interfaceClass interface class whose methods are to be supported by this proxy.
     * @param interfaceImpl  implementation of interfaceClass. Calls that are satisfied by it's interface are delegated to this object.
     * @throws Exception
     */
    public ProxyMethodHandler(Object root, Class<?> interfaceClass, Object interfaceImpl) throws Exception {
        if (!interfaceClass.isAssignableFrom(interfaceImpl.getClass())) {
            throw new IllegalArgumentException("The specified interfaceImpl object must implement the specified interfaceClass!");
        }
        this.contextRoot = root;
        this.interfaceClass = interfaceClass;
        this.interfaceImpl = interfaceImpl;
    }

    @Override
    public Object invoke(Object o, Method method, Method method2, Object[] args) throws Throwable {
        if (methodCanBeHandledByInterface(method)) {
            return method.invoke(interfaceImpl, args);
        } else {
            return method.invoke(contextRoot, args);
        }
    }

    public boolean methodCanBeHandledByInterface(Method method) {
        try {
            interfaceClass.getMethod(method.getName(), method.getParameterTypes());
            // no exception, so we found the method we're looking for
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

}
