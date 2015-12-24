package org.wickedsource.docxstamper;

import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.wickedsource.docxstamper.expression.ExpressionContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Wertet die Kommentare
 */
public class CommentProcessorOld {

    private List<ExpressionContext> contexts = new ArrayList<>();

    public void processComment(XWPFParagraph paragraph, Object daten, String comment) {
        ExpressionParser parser = new SpelExpressionParser();
        ExpressionContext context = new ExpressionContext(paragraph, daten);
        StandardEvaluationContext evaluationContext = new StandardEvaluationContext(context);
        Expression expression = parser.parseExpression(comment);
        Object result = expression.getValue(evaluationContext);
        contexts.add(context);
    }

    public void commitChanges() {
        int indexCorrection = 0;
        for (ExpressionContext context : contexts) {

            if (context.isDeleteParagraph()) {
                XWPFParagraph paragraph = context.getParagraph();
                int pos = paragraph.getDocument().getPosOfParagraph(paragraph);
                // Korrektur des Index notwendig, da vorher bereits vielleicht andere Paragraphen gel�scht wurden und der Index sich dadurch verschiebt
                pos -= indexCorrection;
                paragraph.getDocument().removeBodyElement(pos);
                indexCorrection++;
            }

            if (context.isMakeBold()) {
                XWPFParagraph paragraph = context.getParagraph();
                for (XWPFRun run : paragraph.getRuns()) {
                    run.setBold(true);
                }
            }
        }
    }
}
