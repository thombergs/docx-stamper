package org.wickedsource.docxstamper.processor.repeat;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Tr;
import org.wickedsource.docxstamper.DocxStamperConfiguration;
import org.wickedsource.docxstamper.api.typeresolver.TypeResolverRegistry;
import org.wickedsource.docxstamper.el.ExpressionResolver;
import org.wickedsource.docxstamper.processor.BaseCommentProcessor;
import org.wickedsource.docxstamper.processor.CommentProcessingException;
import org.wickedsource.docxstamper.replace.PlaceholderReplacer;
import org.wickedsource.docxstamper.util.CommentUtil;
import org.wickedsource.docxstamper.util.walk.DocumentWalker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class RepeatProcessor extends BaseCommentProcessor implements IRepeatProcessor {

    private Map<Tr, List<Object>> tableRowsToRepeat = new HashMap<>();

    private final PlaceholderReplacer<Object> placeholderReplacer;

    public RepeatProcessor(
            TypeResolverRegistry typeResolverRegistry,
            ExpressionResolver expressionResolver,
            DocxStamperConfiguration config
    ) {

        this.placeholderReplacer = new PlaceholderReplacer<>(typeResolverRegistry, config.getLineBreakPlaceholder());
        placeholderReplacer.setExpressionResolver(expressionResolver);
        placeholderReplacer.setLeaveEmptyOnExpressionError(config.isLeaveEmptyOnExpressionError());
        placeholderReplacer.setReplaceNullValues(config.isReplaceNullValues());
        placeholderReplacer.setNullValuesDefault(config.getNullValuesDefault());
        placeholderReplacer.setReplaceUnresolvedExpressions(config.isReplaceUnresolvedExpressions());
        placeholderReplacer.setUnresolvedExpressionsDefaultValue(config.getUnresolvedExpressionsDefaultValue());
    }

    @Override
    public void commitChanges(WordprocessingMLPackage document) {
        repeatRows(document);
    }

    @Override
    public void reset() {
        this.tableRowsToRepeat = new HashMap<>();
    }

    private void repeatRows(final WordprocessingMLPackage document) {
        for (Entry<Tr, List<Object>> entry : tableRowsToRepeat.entrySet()) {
            Tr row = entry.getKey();
            List<Object> expressionContexts = entry.getValue();
            for (Object expressionContext : expressionContexts) {
                Tr rowClone = XmlUtils.deepCopy(row);
                DocumentWalker walker = new ParagraphResolverDocumentWalker(rowClone, expressionContext, document, this.placeholderReplacer);
                walker.walk();
                ((Tbl)row.getParent()).getContent().add(rowClone);
            }
            ((Tbl)row.getParent()).getContent().remove(row);
        }
    }

    @Override
    public void repeatTableRow(List<Object> objects) {
        P paragraph = getParagraph();

        Object cell = paragraph.getParent();
        if (!(cell instanceof Tc))
            throw new CommentProcessingException("Paragraph is not within a table!", paragraph);

        Object row = ((Tc) cell).getParent();
        if (!(row instanceof Tr))
            throw new CommentProcessingException("Paragraph is not within a table!", paragraph);

        tableRowsToRepeat.put((Tr) row, objects);
        CommentUtil.deleteComment(getCurrentCommentWrapper());
    }

}
