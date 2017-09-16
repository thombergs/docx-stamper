package org.wickedsource.docxstamper;

import org.wickedsource.docxstamper.api.EvaluationContextConfigurer;
import org.wickedsource.docxstamper.el.NoOpEvaluationContextConfigurer;

/**
 * Provides configuration parameters for DocxStamper.
 */
public class DocxStamperConfiguration {

  private String lineBreakPlaceholder;

  private EvaluationContextConfigurer evaluationContextConfigurer = new NoOpEvaluationContextConfigurer();

  private boolean failOnUnresolvedExpression = true;

  public String getLineBreakPlaceholder() {
    return lineBreakPlaceholder;
  }

  /**
   * The String provided as lineBreakPlaceholder will be replaces with a line break
   * when stamping a document. If no lineBreakPlaceholder is provided, no replacement
   * will take place.
   *
   * @param lineBreakPlaceholder the String that should be replaced with line breaks during stamping.
   * @return the configuration object for chaining.
   */
  public DocxStamperConfiguration setLineBreakPlaceholder(String lineBreakPlaceholder) {
    this.lineBreakPlaceholder = lineBreakPlaceholder;
    return this;
  }

  /**
   * Provides an {@link EvaluationContextConfigurer} which may change the configuration of a Spring
   * {@link org.springframework.expression.EvaluationContext} which is used for evaluating expressions
   * in comments and text.
   * @param evaluationContextConfigurer the configurer to use.
   */
  public void setEvaluationContextConfigurer(EvaluationContextConfigurer evaluationContextConfigurer) {
    this.evaluationContextConfigurer = evaluationContextConfigurer;
  }

  public EvaluationContextConfigurer getEvaluationContextConfigurer() {
    return evaluationContextConfigurer;
  }

  /**
   * If set to true, stamper will throw an {@link org.wickedsource.docxstamper.api.UnresolvedExpressionException}
   * if a variable expression or processor expression within the document or within the comments is encountered that cannot be resolved. Is set to true by default.
   */
  public void setFailOnUnresolvedExpression(boolean failOnUnresolvedExpression) {
    this.failOnUnresolvedExpression = failOnUnresolvedExpression;
  }

  public boolean isFailOnUnresolvedExpression() {
    return failOnUnresolvedExpression;
  }

}
