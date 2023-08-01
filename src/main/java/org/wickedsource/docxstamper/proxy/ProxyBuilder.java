package org.wickedsource.docxstamper.proxy;

import java.util.HashMap;
import java.util.Map;

import javassist.util.proxy.ProxyFactory;

/**
 * Allows an object to be wrapped by a proxy so that it will implement additional interfaces.
 *
 * @param <T> the type of the root object.
 */
public class ProxyBuilder<T> {

  private T root;

  private Map<Class<?>, Object> interfacesToImplementations = new HashMap<>();

  /**
   * Specifies the root object for the proxy that shall be enhanced.
   *
   * @param rootObject the root object.
   * @return this builder for chaining.
   */
  public ProxyBuilder<T> withRoot(T rootObject) {
    this.root = rootObject;
    return this;
  }

  public ProxyBuilder<T> cloneWithNewRoot(T rootObject) {
    ProxyBuilder<T> proxy = new ProxyBuilder<>();
    proxy.root = rootObject;
    proxy.interfacesToImplementations = new HashMap<>(interfacesToImplementations);
    return proxy;
  }

  /**
   * Specifies an interfaces and an implementation of an interface by which the root object
   * shall be extended.
   *
   * @param interfaceClass the class of the interface
   * @param interfaceImpl  an implementation of the interface
   * @return this builder for chaining.
   */
  public ProxyBuilder<T> withInterface(Class<?> interfaceClass, Object interfaceImpl) {
    this.interfacesToImplementations.put(interfaceClass, interfaceImpl);
    return this;
  }

  /**
   * Creates a proxy object out of the specified root object and the specified interfaces
   * and implementations.
   *
   * @return a proxy object that is still of type T but additionally implements all specified
   * interfaces.
   * @throws ProxyException if the proxy could not be created.
   */
  public T build() throws ProxyException {

    if (this.root == null) {
      throw new IllegalArgumentException("root must not be null!");
    }

    if (this.interfacesToImplementations.isEmpty()) {
      // nothing to proxy
      return this.root;
    }

    try {
      ProxyMethodHandler methodHandler = new ProxyMethodHandler(root,
              interfacesToImplementations);
      ProxyFactory proxyFactory = new ProxyFactory();
      proxyFactory.setSuperclass(root.getClass());
      proxyFactory.setInterfaces(interfacesToImplementations.keySet().toArray(new Class[]{}));
      return (T) proxyFactory.create(new Class[0], new Object[0], methodHandler);
    } catch (Exception e) {
      throw new ProxyException(e);
    }
  }


}