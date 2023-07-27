package org.wickedsource.docxstamper;

import org.wickedsource.docxstamper.replace.PlaceholderReplacer;

/**
 * Factory interface for creating {@link org.wickedsource.docxstamper.api.commentprocessor.ICommentProcessor} instances.
 *
 * @author joseph
 * @version $Id: $Id
 */
public interface CommentProcessorBuilder {
    /**
     * Creates a {@link org.wickedsource.docxstamper.api.commentprocessor.ICommentProcessor} instance.
     *
     * @param placeholderReplacer the placeholder replacer that should be used by the comment processor.
     * @return a {@link org.wickedsource.docxstamper.api.commentprocessor.ICommentProcessor} instance.
     */
    Object create(PlaceholderReplacer placeholderReplacer);
}
