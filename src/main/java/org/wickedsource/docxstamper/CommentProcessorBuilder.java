package org.wickedsource.docxstamper;

import org.wickedsource.docxstamper.replace.PlaceholderReplacer;

public interface CommentProcessorBuilder {
    Object create(PlaceholderReplacer placeholderReplacer);
}
