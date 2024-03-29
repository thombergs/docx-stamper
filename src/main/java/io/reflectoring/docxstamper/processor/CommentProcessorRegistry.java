package io.reflectoring.docxstamper.processor;

import io.reflectoring.docxstamper.api.DocxStamperException;
import io.reflectoring.docxstamper.el.ExpressionResolver;
import io.reflectoring.docxstamper.el.ExpressionUtil;
import io.reflectoring.docxstamper.util.CommentWrapper;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Comments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.SpelParseException;
import io.reflectoring.docxstamper.api.UnresolvedExpressionException;
import io.reflectoring.docxstamper.api.commentprocessor.ICommentProcessor;
import io.reflectoring.docxstamper.api.coordinates.ParagraphCoordinates;
import io.reflectoring.docxstamper.api.coordinates.RunCoordinates;
import io.reflectoring.docxstamper.proxy.ProxyBuilder;
import io.reflectoring.docxstamper.proxy.ProxyException;
import io.reflectoring.docxstamper.replace.PlaceholderReplacer;
import io.reflectoring.docxstamper.util.CommentUtil;
import io.reflectoring.docxstamper.util.ParagraphWrapper;
import io.reflectoring.docxstamper.util.walk.BaseCoordinatesWalker;
import io.reflectoring.docxstamper.util.walk.CoordinatesWalker;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Allows registration of ICommentProcessor objects. Each registered
 * ICommentProcessor must implement an interface which has to be specified at
 * registration time. Provides several getter methods to access the registered
 * ICommentProcessors.
 */
public class CommentProcessorRegistry {

  private Logger logger = LoggerFactory.getLogger(CommentProcessorRegistry.class);

  private Map<ICommentProcessor, Class<?>> commentProcessorInterfaces = new HashMap<>();

  private List<ICommentProcessor> commentProcessors = new ArrayList<>();

  private ExpressionResolver expressionResolver = new ExpressionResolver();

  private ExpressionUtil expressionUtil = new ExpressionUtil();

  private PlaceholderReplacer placeholderReplacer;

  private boolean failOnInvalidExpression = true;

  public CommentProcessorRegistry(PlaceholderReplacer placeholderReplacer) {
    this.placeholderReplacer = placeholderReplacer;
  }

  public void setExpressionResolver(ExpressionResolver expressionResolver) {
    this.expressionResolver = expressionResolver;
  }

  public void registerCommentProcessor(Class<?> interfaceClass,
                                       ICommentProcessor commentProcessor) {
    this.commentProcessorInterfaces.put(commentProcessor, interfaceClass);
    this.commentProcessors.add(commentProcessor);
  }

  /**
   * Lets each registered ICommentProcessor have a run on the specified docx
   * document. At the end of the document the commit method is called for each
   * ICommentProcessor. The ICommentProcessors are run in the order they were
   * registered.
   *
   * @param document    the docx document over which to run the registered ICommentProcessors.
   * @param proxyBuilder a builder for a proxy around the context root object to customize its interface
   * @param <T>         type of the contextRoot object.
   */
  public <T> void runProcessors(final WordprocessingMLPackage document, final ProxyBuilder<T> proxyBuilder) {
    final Map<BigInteger, CommentWrapper> comments = CommentUtil.getComments(document);
    final List<CommentWrapper> proceedComments = new ArrayList<>();

    CoordinatesWalker walker = new BaseCoordinatesWalker(document) {

      @Override
      protected void onParagraph(ParagraphCoordinates paragraphCoordinates) {
        runProcessorsOnParagraphComment(document, comments, proxyBuilder, paragraphCoordinates)
                .ifPresent(proceedComments::add);
        runProcessorsOnInlineContent(proxyBuilder, paragraphCoordinates);
      }

      @Override
      protected void onRun(RunCoordinates runCoordinates,
                                     ParagraphCoordinates paragraphCoordinates) {
        runProcessorsOnRunComment(document, comments, proxyBuilder, paragraphCoordinates, runCoordinates)
                .ifPresent(proceedComments::add);
      }

    };
    walker.walk();

    for (ICommentProcessor processor : commentProcessors) {
      processor.commitChanges(document);
    }
    for (CommentWrapper commentWrapper : proceedComments) {
      CommentUtil.deleteComment(commentWrapper);
    }

  }

  /**
   * Finds all processor expressions within the specified paragraph and tries
   * to evaluate it against all registered {@link ICommentProcessor}s.
   *
   * @param proxyBuilder         a builder for a proxy around the context root object to customize its interface
   * @param paragraphCoordinates the paragraph to process.
   * @param <T>                  type of the context root object
   */
  private <T> void runProcessorsOnInlineContent(ProxyBuilder<T> proxyBuilder,
                                                ParagraphCoordinates paragraphCoordinates) {

    ParagraphWrapper paragraph = new ParagraphWrapper(paragraphCoordinates.getParagraph());
    List<String> processorExpressions = expressionUtil
            .findProcessorExpressions(paragraph.getText());

    for (String processorExpression : processorExpressions) {
      String strippedExpression = expressionUtil.stripExpression(processorExpression);

      for (final ICommentProcessor processor : commentProcessors) {
        Class<?> commentProcessorInterface = commentProcessorInterfaces.get(processor);
        proxyBuilder.withInterface(commentProcessorInterface, processor);
        processor.setCurrentParagraphCoordinates(paragraphCoordinates);
        processor.setProxyBuilder(proxyBuilder);
      }

      try {
        T contextRootProxy = proxyBuilder.build();
        expressionResolver.resolveExpression(strippedExpression, contextRootProxy);
        placeholderReplacer.replace(paragraph, processorExpression, null);
        logger.debug(String.format(
                "Processor expression '%s' has been successfully processed by a comment processor.",
                processorExpression));
      } catch (SpelEvaluationException | SpelParseException e) {
        if (failOnInvalidExpression) {
          throw new UnresolvedExpressionException(strippedExpression, e);
        } else {
          logger.warn(String.format(
                  "Skipping processor expression '%s' because it can not be resolved by any comment processor. Reason: %s. Set log level to TRACE to view Stacktrace.",
                  processorExpression, e.getMessage()));
          logger.trace("Reason for skipping processor expression: ", e);
        }
      } catch (ProxyException e) {
        throw new DocxStamperException("Could not create a proxy around context root object", e);
      }
    }
  }


  /**
   * Takes the first comment on the specified paragraph and tries to evaluate
   * the string within the comment against all registered
   * {@link ICommentProcessor}s.
   *
   * @param document             the word document.
   * @param comments             the comments within the document.
   * @param proxyBuilder          a builder for a proxy around the context root object to customize its interface
   * @param paragraphCoordinates the paragraph whose comments to evaluate.
   * @param <T>                  the type of the context root object.
   */
  private <T> Optional<CommentWrapper> runProcessorsOnParagraphComment(final WordprocessingMLPackage document,
                                                   final Map<BigInteger, CommentWrapper> comments, ProxyBuilder<T> proxyBuilder,
                                                   ParagraphCoordinates paragraphCoordinates) {
    Comments.Comment comment = CommentUtil.getCommentFor(paragraphCoordinates.getParagraph(), document);
    return runCommentProcessors(document, comments, proxyBuilder, comment, paragraphCoordinates, null);
  }

  private <T> Optional<CommentWrapper> runProcessorsOnRunComment(final WordprocessingMLPackage document,
                                                       final Map<BigInteger, CommentWrapper> comments, ProxyBuilder<T> proxyBuilder,
                                                       ParagraphCoordinates paragraphCoordinates, RunCoordinates runCoordinates) {
    Comments.Comment comment = CommentUtil.getCommentAround(runCoordinates.getRun(), document);
    return runCommentProcessors(document, comments, proxyBuilder, comment, paragraphCoordinates, runCoordinates);
  }

  private <T> Optional<CommentWrapper> runCommentProcessors(final WordprocessingMLPackage document,
                                                           final Map<BigInteger, CommentWrapper> comments, ProxyBuilder<T> proxyBuilder,
                                                            Comments.Comment comment, ParagraphCoordinates paragraphCoordinates,
                                                           RunCoordinates runCoordinates) {

    CommentWrapper commentWrapper = Optional.ofNullable(comment)
            .map(Comments.Comment::getId)
            .map(comments::get)
            .orElse(null);

    if (Objects.isNull(comment) || Objects.isNull(commentWrapper)) {
      // no comment to process
      return Optional.empty();
    }

    String commentString = CommentUtil.getCommentString(comment);

    for (final ICommentProcessor processor : commentProcessors) {
      Class<?> commentProcessorInterface = commentProcessorInterfaces.get(processor);
      proxyBuilder.withInterface(commentProcessorInterface, processor);
      processor.setCurrentParagraphCoordinates(paragraphCoordinates);
      processor.setCurrentRunCoordinates(runCoordinates);
      processor.setCurrentCommentWrapper(commentWrapper);
      processor.setProxyBuilder(proxyBuilder);
    }

    try {
      T contextRootProxy = proxyBuilder.build();
      expressionResolver.resolveExpression(commentString, contextRootProxy);
      comments.remove(comment.getId()); // guarantee one-time processing
      logger.debug(
              String.format("Comment '%s' has been successfully processed by a comment processor.",
                      commentString));
      return Optional.of(commentWrapper);
    } catch (SpelEvaluationException | SpelParseException e) {
      if (failOnInvalidExpression) {
        throw new UnresolvedExpressionException(commentString, e);
      } else {
        logger.warn(String.format(
                "Skipping comment expression '%s' because it can not be resolved by any comment processor. Reason: %s. Set log level to TRACE to view Stacktrace.",
                commentString, e.getMessage()));
        logger.trace("Reason for skipping comment: ", e);
      }
    } catch (ProxyException e) {
      throw new DocxStamperException("Could not create a proxy around context root object", e);
    }
    return Optional.empty();
  }

  public boolean isFailOnInvalidExpression() {
    return failOnInvalidExpression;
  }

  public void setFailOnInvalidExpression(boolean failOnInvalidExpression) {
    this.failOnInvalidExpression = failOnInvalidExpression;
  }

  public void reset() {
    for (ICommentProcessor processor : commentProcessors) {
      processor.reset();
    }
  }
}
