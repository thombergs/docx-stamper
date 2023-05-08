package org.wickedsource.docxstamper.api.typeresolver;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry for all implementations of ITypeResolver that are used by DocxStamper.
 */
public class TypeResolverRegistry {

	private final Map<Class<?>, ITypeResolver<?>> typeResolversByType = new HashMap<>();
	private final ITypeResolver<Object> defaultResolver;

	public TypeResolverRegistry(ITypeResolver<Object> defaultResolver) {
		this.defaultResolver = defaultResolver;
	}

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

	public ITypeResolver<Object> getDefaultResolver() {
		return defaultResolver;
	}
}
