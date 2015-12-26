package org.wickedsource.docxstamper.docx4j;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.wickedsource.docxstamper.docx4j.walk.DocumentWalker;
import org.wickedsource.docxstamper.docx4j.walk.coordinates.ParagraphCoordinates;
import org.wickedsource.docxstamper.docx4j.walk.coordinates.TableCellCoordinates;
import org.wickedsource.docxstamper.docx4j.walk.coordinates.TableCoordinates;
import org.wickedsource.docxstamper.docx4j.walk.coordinates.TableRowCoordinates;
import org.wickedsource.docxstamper.el.ExpressionResolver;
import org.wickedsource.docxstamper.el.ExpressionUtil;

import java.util.List;

public class PlaceholderReplacer<T> {

    private ExpressionUtil expressionUtil = new ExpressionUtil();

    private ExpressionResolver expressionResolver = new ExpressionResolver();

    /**
     * Finds expressions in a document and resolves them against the specified context object. The expressions in the
     * document are then replaced by the resolved values.
     *
     * @param document          the document in which to replace expressions.
     * @param expressionContext the context to resolve the expressions against.
     */
    public void resolveExpressions(WordprocessingMLPackage document, final T expressionContext) {
        DocumentWalker walker = new DocumentWalker(document) {
            @Override
            protected void onParagraph(ParagraphCoordinates paragraphCoordinates) {
                resolveExpressionsForParagraph(paragraphCoordinates.getParagraph(), expressionContext);
            }

            @Override
            protected void onTable(TableCoordinates tableCoordinates) {

            }

            @Override
            protected void onTableCell(TableCellCoordinates tableCellCoordinates) {

            }

            @Override
            protected void onTableRow(TableRowCoordinates tableRowCoordinates) {

            }
        };
        walker.walk();
    }

    private void resolveExpressionsForParagraph(P p, T expressionContext) {
        RunAggregator aggregator = new RunAggregator();
        for (Object contentElement : p.getContent()) {
            if (contentElement instanceof R) {
                aggregator.addRun((R) contentElement);
            }
        }
        List<String> placeholders = expressionUtil.findExpressions(aggregator.getText());
        for (String placeholder : placeholders) {
            Object replacement = expressionResolver.resolveExpression(placeholder, expressionContext);
            if (replacement != null) {
                aggregator.replaceFirst(placeholder, String.valueOf(replacement)); //TODO: support images
            }
        }
    }

}
