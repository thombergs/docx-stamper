package pro.verron.docxstamper.commentProcessors;

import org.wickedsource.docxstamper.api.commentprocessor.ICommentProcessor;

public interface ICustomCommentProcessor extends ICommentProcessor {
    void visitParagraph();
}
