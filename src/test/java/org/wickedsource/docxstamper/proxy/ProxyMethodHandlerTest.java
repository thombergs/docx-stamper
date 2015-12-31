package org.wickedsource.docxstamper.proxy;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.wickedsource.docxstamper.context.NameContext;
import org.wickedsource.docxstamper.processor.CommentProcessorRegistry;

public class ProxyMethodHandlerTest {

    private ContextFactory contextFactory = new ContextFactory();

    @Test
    public void proxyDelegatesToRegisteredCommentProcessors() throws Exception {

        CommentProcessorRegistry processorRegistry = new CommentProcessorRegistry();
        processorRegistry.registerCommentProcessor(ITestInterface.class, new TestImpl());

        NameContext contextRoot = new NameContext();
        contextRoot.setName("Tom");
        NameContext context = (NameContext) contextFactory.createProxy(contextRoot, ITestInterface.class, new TestImpl());

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