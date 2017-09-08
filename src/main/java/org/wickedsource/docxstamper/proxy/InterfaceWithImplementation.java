package org.wickedsource.docxstamper.proxy;

import org.wickedsource.docxstamper.api.commentprocessor.ICommentProcessor;

class InterfaceWithImplementation {

	private final Class<?> interfaceClass;

	private final ICommentProcessor implementation;

	public InterfaceWithImplementation(Class<?> interfaceClass,
									   ICommentProcessor implementation) {
		this.interfaceClass = interfaceClass;
		this.implementation = implementation;
	}

	public Class<?> getInterfaceClass() {
		return interfaceClass;
	}

	public ICommentProcessor getImplementation() {
		return implementation;
	}
}
