package org.wickedsource.docxstamper.replace.string;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.R;
import org.wickedsource.docxstamper.api.typeresolver.ITypeResolver;
import org.wickedsource.docxstamper.util.RunUtil;

/**
 * This ITypeResolver resolves objects of all types to their String equivalent by calling String.valueOf(). The result
 * will be put into a run of text in place of the expression that returned the object.
 */
public class StringResolver implements ITypeResolver {

    @Override
    public R resolve(WordprocessingMLPackage document, Object expressionResult) {
        R run = RunUtil.create(String.valueOf(expressionResult));
        return run;
    }

}
