package pro.verron.docxstamper.resolver;

import org.wickedsource.docxstamper.replace.typeresolver.AbstractToTextResolver;
import pro.verron.docxstamper.utils.context.Contexts;

public class CustomTypeResolver extends AbstractToTextResolver<Contexts.CustomType> {
    @Override
    protected String resolveStringForObject(Contexts.CustomType object) {
        return "foo";
    }
}
