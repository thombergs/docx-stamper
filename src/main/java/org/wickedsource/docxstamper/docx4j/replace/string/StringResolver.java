package org.wickedsource.docxstamper.docx4j.replace.string;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.R;
import org.wickedsource.docxstamper.docx4j.replace.TypeResolver;
import org.wickedsource.docxstamper.docx4j.util.RunUtil;

public class StringResolver implements TypeResolver {

    @Override
    public R resolve(WordprocessingMLPackage document, Object expressionResult) {
        R run = RunUtil.create(String.valueOf(expressionResult));
        return run;
    }

}
