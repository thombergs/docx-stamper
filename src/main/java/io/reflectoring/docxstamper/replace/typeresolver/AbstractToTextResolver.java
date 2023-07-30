package io.reflectoring.docxstamper.replace.typeresolver;

import io.reflectoring.docxstamper.api.typeresolver.ITypeResolver;
import io.reflectoring.docxstamper.util.RunUtil;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.R;

/**
 * Abstract ITypeResolver that takes a String from the implementing sub class and creates a Run of text
 * from it.
 *
 * @param <S> the type which to map into a run of text.
 */
public abstract class AbstractToTextResolver<S> implements ITypeResolver<S, R> {

    protected abstract String resolveStringForObject(S object);

    @Override
    public R resolve(WordprocessingMLPackage document, S expressionResult) {
        String text = resolveStringForObject(expressionResult);
        R run = RunUtil.create(text);
        return run;
    }
}
