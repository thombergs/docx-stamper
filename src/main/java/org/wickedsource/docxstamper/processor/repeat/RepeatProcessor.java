package org.wickedsource.docxstamper.processor.repeat;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.*;
import org.wickedsource.docxstamper.DocxStamperConfiguration;
import org.wickedsource.docxstamper.api.typeresolver.TypeResolverRegistry;
import org.wickedsource.docxstamper.processor.BaseCommentProcessor;
import org.wickedsource.docxstamper.processor.CommentProcessingException;
import org.wickedsource.docxstamper.util.CommentUtil;
import org.wickedsource.docxstamper.util.CommentWrapper;
import org.wickedsource.docxstamper.util.walk.BaseDocumentWalker;
import org.wickedsource.docxstamper.util.walk.DocumentWalker;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RepeatProcessor extends BaseCommentProcessor implements IRepeatProcessor {

	private Map<Tr, List<Object>> tableRowsToRepeat = new HashMap<>();
	private Map<Tr, CommentWrapper> tableRowsCommentsToRemove = new HashMap<>();

	public RepeatProcessor(
			DocxStamperConfiguration config,
			TypeResolverRegistry typeResolverRegistry
	) {
		super(config, typeResolverRegistry);
	}


	@Override
	public void commitChanges(WordprocessingMLPackage document) {
		repeatRows(document);
	}

	@Override
	public void reset() {
		this.tableRowsToRepeat = new HashMap<>();
		this.tableRowsCommentsToRemove = new HashMap<>();
	}

	private void repeatRows(final WordprocessingMLPackage document) {
		for (Tr row : tableRowsToRepeat.keySet()) {
			List<Object> expressionContexts = tableRowsToRepeat.get(row);
			Tbl table = (Tbl) XmlUtils.unwrap(row.getParent());
			int index = table.getContent().indexOf(row);

			if (expressionContexts != null) {
				for (final Object expressionContext : expressionContexts) {
					Tr rowClone = XmlUtils.deepCopy(row);
					CommentWrapper commentWrapper = Objects.requireNonNull(tableRowsCommentsToRemove.get(row));
					Comments.Comment comment = Objects.requireNonNull(commentWrapper.getComment());
					BigInteger commentId = comment.getId();
					CommentUtil.deleteCommentFromElement(rowClone, commentId);

					DocumentWalker walker = new BaseDocumentWalker(rowClone) {
						@Override
						protected void onParagraph(P paragraph) {
							placeholderReplacer.resolveExpressionsForParagraph(paragraph, expressionContext, document);
						}
					};
					walker.walk();
					table.getContent().add(++index, rowClone);
				}
			} else if (configuration.isReplaceNullValues() && configuration.getNullValuesDefault() != null) {
				Tr rowClone = XmlUtils.deepCopy(row);
				Object nullExpressionContext = new Object();
				DocumentWalker walker = new ParagraphResolverDocumentWalker(rowClone,
																			nullExpressionContext,
																			document,
																			this.placeholderReplacer);
				walker.walk();
				((Tbl) row.getParent()).getContent().add(rowClone);
			}
			table.getContent().remove(row);
		}
	}

	@Override
	public void repeatTableRow(List<Object> objects) {
		P pCoords = getParagraph();

		if (pCoords.getParent() instanceof Tc tc
				&& tc.getParent() instanceof Tr tableRow) {
			tableRowsToRepeat.put(tableRow, objects);
			tableRowsCommentsToRemove.put(tableRow, getCurrentCommentWrapper());
		} else {
			throw new CommentProcessingException("Paragraph is not within a table!", pCoords);
		}
	}
}
