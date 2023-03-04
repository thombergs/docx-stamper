package org.wickedsource.docxstamper.replace.typeresolver;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.R;
import org.wickedsource.docxstamper.api.typeresolver.ITypeResolver;
import org.wickedsource.docxstamper.util.RunUtil;

/**
 * Abstract ITypeResolver that takes a String from the implementing sub class and creates a Run of text
 * from it.
 *
 * @param <S> the type which to map into a run of text.
 */
public abstract class AbstractToTextResolver<S> implements ITypeResolver<S> {

	protected AbstractToTextResolver() {
	}

	@Override
	public R resolve(WordprocessingMLPackage document, S expressionResult) {
		String text = resolveStringForObject(expressionResult);
		return RunUtil.create(text);
	}

	protected abstract String resolveStringForObject(S object);
}
