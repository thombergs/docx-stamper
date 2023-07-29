package org.wickedsource.docxstamper.api.typeresolver;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry for all implementations of ITypeResolver that are used by DocxStamper.
 *
 * @author joseph
 * @version $Id: $Id
 */
public class TypeResolverRegistry {
	private final Map<Class<?>, ITypeResolver<?>> typeResolversByType = new HashMap<>();

	private final ITypeResolver<Object> defaultResolver;

	/**
	 * Creates a new TypeResolverRegistry with the given default ITypeResolver.
	 *
	 * @param defaultResolver the ITypeResolver that is used when no specific resolver for a type is registered.
	 */
	public TypeResolverRegistry(ITypeResolver<Object> defaultResolver) {
		this.defaultResolver = defaultResolver;
	}

	/**
	 * Registers a new ITypeResolver for the given type.
	 *
	 * @param resolvedType the type for which the resolver is registered.
	 * @param resolver     the ITypeResolver implementation to register.
	 * @param <T>          the type resolved by the ITypeResolver.
	 */
	public <T> void registerTypeResolver(Class<T> resolvedType, ITypeResolver<T> resolver) {
		typeResolversByType.put(resolvedType, resolver);
	}

	/**
	 * Gets the ITypeResolver that was registered for the specified type.
	 *
	 * @param type the class for which to find the ITypeResolver.
	 * @param <T>  the type resolved by the ITypeResolver.
	 * @return the ITypeResolver implementation that was earlier registered for the given class, or the default ITypeResolver
	 * if none is found.
	 */
	public <T> ITypeResolver<T> getResolverForType(Class<T> type) {
		return typeResolversByType.containsKey(type)
				? (ITypeResolver<T>) typeResolversByType.get(type)
				: (ITypeResolver<T>) defaultResolver;
	}

	/**
	 * <p>Getter for the field <code>defaultResolver</code>.</p>
	 *
	 * @return a {@link org.wickedsource.docxstamper.api.typeresolver.ITypeResolver} object
	 */
	public ITypeResolver<Object> getDefaultResolver() {
		return defaultResolver;
	}
}
