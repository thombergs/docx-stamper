package org.wickedsource.docxstamper.processor.replaceExpression;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wickedsource.docxstamper.DocxStamperConfiguration;
import org.wickedsource.docxstamper.processor.BaseCommentProcessor;
import org.wickedsource.docxstamper.util.RunUtil;

/**
 * @deprecated
 */
@Deprecated
public class ReplaceWithProcessor extends BaseCommentProcessor
        implements IReplaceWithProcessor {

    private final Logger logger = LoggerFactory.getLogger(ReplaceWithProcessor.class);

    private final DocxStamperConfiguration config;

    public ReplaceWithProcessor(DocxStamperConfiguration config) {
        this.config = config;
    }

    @Override
    public void commitChanges(WordprocessingMLPackage document) {
    }

    @Override
    public void reset() {
        // nothing to rest
    }

    @Override
    public void replaceWordWith(String expression) {
        logger.warn("replaceWordWith has been deprecated in favor of inplace placeholders (${expression})");
        if (expression != null && this.getCurrentRunCoordinates() != null) {
            RunUtil.setText(this.getCurrentRunCoordinates().getRun(), expression);
        } else if (config.isReplaceNullValues() && config.getNullValuesDefault() != null) {
            RunUtil.setText(this.getCurrentRunCoordinates().getRun(), config.getNullValuesDefault());
        }
    }
}
