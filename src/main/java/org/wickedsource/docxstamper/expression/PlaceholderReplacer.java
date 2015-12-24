package org.wickedsource.docxstamper.expression;

import org.apache.poi.xwpf.usermodel.*;
import org.wickedsource.docxstamper.RunAggregator;

import java.util.List;

public class PlaceholderReplacer<T> {

    private ExpressionUtil expressionUtil = new ExpressionUtil();

    private ExpressionResolver expressionResolver = new ExpressionResolver();

    /**
     * Finds expressions in a XWPFDocument and resolves them against the specified context object. The expressions in the
     * document are then replaced by the resolved values.
     *
     * @param document          the document in which to replace expressions.
     * @param expressionContext the context to resolve the expressions against.
     */
    public void resolveExpressions(XWPFDocument document, T expressionContext) {
        // going through all paragraphs of the document
        for (XWPFParagraph p : document.getParagraphs()) {
            resolveExpressionsForParagraph(p, expressionContext);
        }

        // going through all tables of the document
        for (XWPFTable table : document.getTables()) {
            resolveExpressionsForTable(table, expressionContext);
        }
    }

    private void resolveExpressionsForTable(XWPFTable table, T expressionContext) {
        for (XWPFTableRow row : table.getRows()) {
            for (XWPFTableCell cell : row.getTableCells()) {
                for (XWPFParagraph p : cell.getParagraphs()) {
                    resolveExpressionsForParagraph(p, expressionContext);
                }
                for (XWPFTable nestedTable : cell.getTables()) {
                    resolveExpressionsForTable(nestedTable, expressionContext);
                }
            }
        }
    }

    private void resolveExpressionsForParagraph(XWPFParagraph p, T expressionContext) {
        List<XWPFRun> runs = p.getRuns();
        if (runs != null) {
            RunAggregator aggregator = new RunAggregator();
            for (XWPFRun r : runs) {
                aggregator.addRun(r);
            }
            List<String> placeholders = expressionUtil.findExpressions(aggregator.getText());
            for (String placeholder : placeholders) {
                String replacement = String.valueOf(expressionResolver.resolveExpression(placeholder, expressionContext));
                aggregator.replaceFirst(placeholder, replacement);
            }
        }
    }

}
