package org.wickedsource.docxstamper;

import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTMarkupRange;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.wickedsource.docxstamper.expression.ExpressionUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public abstract class WordDocumentGenerator<T> {

    private InputStream template;

    private CommentProcessorOld commentProcessorOld = new CommentProcessorOld();

    private ExpressionUtil expressionUtil = new ExpressionUtil();

    public WordDocumentGenerator(InputStream template) {
        this.template = template;
    }

    public void generate(TemplateMapping<T> mapping, T data, OutputStream out) throws IOException {
        XWPFDocument doc = new XWPFDocument(template);
        beforePlaceholderReplacement(doc, data);
        processComments(doc, mapping, data);
        replacePlaceholders(doc, mapping, data);
        afterPlaceholderReplacement(doc, data);
        doc.write(out);
    }

    /**
     * Kann �berschrieben werden, um das Word-Dokument vor der Ersetzung der Platzhalter zu modifizieren.
     */
    protected void beforePlaceholderReplacement(XWPFDocument doc, T data) {

    }

    /**
     * Kann �berschrieben werden, um das Word-Dokument nach der Ersetzung der Platzhalter zu modifizieren.
     */
    protected void afterPlaceholderReplacement(XWPFDocument doc, T data) {

    }

    protected void processComments(XWPFDocument doc, TemplateMapping<T> mapping, T data){
        for (XWPFParagraph p : doc.getParagraphs()) {
            List<CTMarkupRange> markupRangeList = p.getCTP().getCommentRangeStartList();
            for (CTMarkupRange anchor : markupRangeList) {
                XWPFComment comment = p.getDocument().getCommentByID(anchor.getId().toString());
                commentProcessorOld.processComment(p, data, comment.getText());
            }
        }

        commentProcessorOld.commitChanges();

        // TODO: Kommentare aus Dokument l�schen
    }

    protected void replacePlaceholders(XWPFDocument doc, TemplateMapping<T> mapping, T data) {
        for (XWPFParagraph p : doc.getParagraphs()) {
            List<XWPFRun> runs = p.getRuns();
            if (runs != null) {
                for (XWPFRun r : runs) {
                    String text = r.getText(0);
                    // TODO: Placeholderersetzung �ber mehrere Runs!!!
                    for (String placeholder : expressionUtil.findExpressions(text)) {
                        String expressionString = placeholder.replaceAll("%%%", "");
                        if (text != null && text.contains(placeholder)) {
                            ExpressionParser parser = new SpelExpressionParser();
                            StandardEvaluationContext evaluationContext = new StandardEvaluationContext(data); // TODO: selber Context wie in Kommentaren
                            Expression expression = parser.parseExpression(expressionString);
                            Object result = expression.getValue(evaluationContext); // TODO: mit Image und Date usw. umgehen
                            text = text.replace(placeholder, result.toString());
                            r.setText(text, 0);
                        }
                    }
                }
            }
        }

        for (XWPFTable tbl : doc.getTables()) {
            for (XWPFTableRow row : tbl.getRows()) {
                for (XWPFTableCell cell : row.getTableCells()) {
                    for (XWPFParagraph p : cell.getParagraphs()) {
                        for (XWPFRun r : p.getRuns()) {
                            String text = r.getText(0);
                            // TODO: Placeholderersetzung �ber mehrere Runs!!!
                            for (String placeholder : expressionUtil.findExpressions(text)) {
                                String expressionString = placeholder.replaceAll("%%%", "");
                                if (text != null && text.contains(placeholder)) {
                                    ExpressionParser parser = new SpelExpressionParser();
                                    StandardEvaluationContext evaluationContext = new StandardEvaluationContext(data); // TODO: selber Context wie in Kommentaren
                                    Expression expression = parser.parseExpression(expressionString);
                                    Object result = expression.getValue(evaluationContext);
                                    text = text.replace(placeholder, result.toString());
                                    r.setText(text, 0);
                                }
                            }
                        }
                    }
                }
            }
        }
    }



}
