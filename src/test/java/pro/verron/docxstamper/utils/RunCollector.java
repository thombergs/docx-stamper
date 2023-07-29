package pro.verron.docxstamper.utils;

import org.docx4j.utils.TraversalUtilVisitor;
import org.docx4j.wml.R;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Stream;

/**
 * <p>RunCollector class.</p>
 *
 * @author joseph
 * @version $Id: $Id
 * @since 1.6.5
 */
public class RunCollector extends TraversalUtilVisitor<R> {
	private final Set<R> paragraphs = new LinkedHashSet<>();

	/**
	 * <p>runs.</p>
	 *
	 * @return a {@link java.util.stream.Stream} object
	 */
	public Stream<R> runs() {
		return paragraphs.stream();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void apply(R paragraph) {
		paragraphs.add(paragraph);
	}
}
