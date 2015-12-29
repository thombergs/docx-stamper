package org.wickedsource.docxstamper.docx4j.replace;

import java.util.HashMap;
import java.util.Map;

public class TypeResolverRegistry {

    private TypeResolver defaultResolver;

    private Map<Class<?>, TypeResolver> typeResolversByType = new HashMap<>();

    public TypeResolverRegistry(TypeResolver defaultResolver) {
        this.defaultResolver = defaultResolver;
    }

    public <T> void registerTypeResolver(Class<T> resolvedType, TypeResolver resolver) {
        typeResolversByType.put(resolvedType, resolver);
    }

    public <T> TypeResolver getResolverForType(Class<T> type) {
        TypeResolver resolver = typeResolversByType.get(type);
        if (resolver == null) {
            return defaultResolver;
        } else {
            return resolver;
        }
    }

    public void setDefaultResolver(TypeResolver defaultResolver) {
        this.defaultResolver = defaultResolver;
    }
}
