package org.wickedsource.docxstamper.replace.string;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.R;
import org.wickedsource.docxstamper.replace.TypeResolver;
import org.wickedsource.docxstamper.util.RunUtil;

public class StringResolver implements TypeResolver {

    @Override
    public R resolve(WordprocessingMLPackage document, Object expressionResult) {
        R run = RunUtil.create(String.valueOf(expressionResult));
        return run;
    }

}
