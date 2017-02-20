package org.wickedsource.docxstamper.processor.replaceExpression;

public interface IReplaceExpressionProcessor {

    /**
     * May be called to mark a text inside a paragraph using a comment to replace the expression
     *
     * @param expression the expression to replace with
     */
    void replaceExpression(String expression);

}
