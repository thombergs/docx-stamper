package org.wickedsource.docxstamper.processor.replaceExpression;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.R;
import org.wickedsource.docxstamper.api.DocxStamperException;
import org.wickedsource.docxstamper.api.commentprocessor.ICommentProcessor;
import org.wickedsource.docxstamper.processor.BaseCommentProcessor;
import org.wickedsource.docxstamper.replace.PlaceholderReplacer;
import org.wickedsource.docxstamper.util.RunUtil;

import java.util.List;
import java.util.function.Function;

import static java.lang.String.format;

/**
 * Processor that replaces the current run with the provided expression.
 * This is useful for replacing an expression in a comment with the result of the expression.
 *
 * @author joseph
 * @version $Id: $Id
 */
public class ReplaceWithProcessor
		extends BaseCommentProcessor
		implements IReplaceWithProcessor {

	private final Function<R, List<Object>> nullSupplier;

	private ReplaceWithProcessor(
			PlaceholderReplacer placeholderReplacer,
			Function<R, List<Object>> nullSupplier
	) {
		super(placeholderReplacer);
		this.nullSupplier = nullSupplier;
	}

    /**
     * Creates a new processor that replaces the current run with the result of the expression.
     *
     * @param pr                   the placeholder replacer to use
     * @param nullReplacementValue a {@link java.lang.String} object
     * @return the processor
     */
	public static ICommentProcessor newInstance(PlaceholderReplacer pr, String nullReplacementValue) {
		return new ReplaceWithProcessor(pr, run -> List.of(RunUtil.createText(nullReplacementValue)));
    }

    /**
     * Creates a new processor that replaces the current run with the result of the expression.
     *
     * @param pr the placeholder replacer to use
     * @return the processor
	 */
	public static ICommentProcessor newInstance(PlaceholderReplacer pr) {
		return new ReplaceWithProcessor(pr, R::getContent);
    }

    /** {@inheritDoc} */
	@Override
	public void commitChanges(WordprocessingMLPackage document) {
		// nothing to commit
    }

    /** {@inheritDoc} */
	@Override
	public void reset() {
		// nothing to reset
    }

    /** {@inheritDoc} */
	@Override
	public void replaceWordWith(String expression) {
		R run = this.getCurrentRun();
		if (run == null)
			throw new DocxStamperException(format("Impossible to put expression %s in a null run", expression));

		List<Object> target;
		if (expression != null) {
			target = List.of(RunUtil.createText(expression));
		} else {
			target = nullSupplier.apply(run);
		}
		run.getContent().clear();
		run.getContent().addAll(target);
	}
}
