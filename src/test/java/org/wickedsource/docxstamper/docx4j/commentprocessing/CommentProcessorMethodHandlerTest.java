package org.wickedsource.docxstamper.docx4j.commentprocessing;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.wickedsource.docxstamper.docx4j.TestContext;
import org.wickedsource.docxstamper.docx4j.processor.CommentProcessorRegistry;
import org.wickedsource.docxstamper.docx4j.processor.ContextFactory;

public class CommentProcessorMethodHandlerTest {

    private ContextFactory contextFactory = new ContextFactory();

    @Test
    public void proxyDelegatesToRegisteredCommentProcessors() throws Exception {

        CommentProcessorRegistry processorRegistry = new CommentProcessorRegistry();
        processorRegistry.registerCommentProcessor(ITestInterface.class, new TestImpl());

        TestContext contextRoot = new TestContext();
        contextRoot.setName("Tom");
        TestContext context = (TestContext) contextFactory.createProxy(contextRoot, processorRegistry);

        ExpressionParser parser = new SpelExpressionParser();
        StandardEvaluationContext evaluationContext = new StandardEvaluationContext(context);

        Expression nameExpression = parser.parseExpression("name");
        String name = (String) nameExpression.getValue(evaluationContext);

        Expression methodExpression = parser.parseExpression("returnString(name)");
        String returnStringResult = (String) methodExpression.getValue(evaluationContext);

        Assert.assertEquals("Tom", returnStringResult);
        Assert.assertEquals("Tom", name);
    }

}