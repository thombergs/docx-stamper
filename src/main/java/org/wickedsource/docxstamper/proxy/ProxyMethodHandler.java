package org.wickedsource.docxstamper.proxy;

import java.lang.reflect.Method;
import java.util.Map;

import javassist.util.proxy.MethodHandler;

public class ProxyMethodHandler implements MethodHandler {

  private final Object contextRoot;

  private final Map<Class<?>, Object> interfacesWithImplementations;

  public ProxyMethodHandler(Object root,
                            Map<Class<?>, Object> interfacesWithImplementations) {
    this.contextRoot = root;
    this.interfacesWithImplementations = interfacesWithImplementations;
    for (Map.Entry<Class<?>, Object> entry : interfacesWithImplementations.entrySet()) {
      Class<?> interfaceClass = entry.getKey();
      Object implementation = entry.getValue();
      if (!interfaceClass.isAssignableFrom(implementation.getClass())) {
        throw new IllegalArgumentException(
                String.format("%s does not implement %s!", implementation, interfaceClass));
      }
    }
  }

  @Override
  public Object invoke(Object o, Method method, Method method2, Object[] args) throws Throwable {
    for (Map.Entry<Class<?>, Object> entry : interfacesWithImplementations.entrySet()) {
      Class<?> interfaceClass = entry.getKey();
      Object implementation = entry.getValue();
      if (methodCanBeHandledByInterface(method, interfaceClass)) {
        return method.invoke(implementation, args);
      }
    }

    return method.invoke(contextRoot, args);
  }

  public boolean methodCanBeHandledByInterface(Method method, Class<?> interfaceClass) {
    try {
      interfaceClass.getMethod(method.getName(), method.getParameterTypes());
      // no exception, so we found the method we're looking for
      return true;
    } catch (NoSuchMethodException e) {
      return false;
    }
  }

}
