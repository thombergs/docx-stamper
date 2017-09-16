package org.wickedsource.docxstamper.proxy;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.wickedsource.docxstamper.api.typeresolver.TypeResolverRegistry;
import org.wickedsource.docxstamper.context.NameContext;
import org.wickedsource.docxstamper.processor.CommentProcessorRegistry;
import org.wickedsource.docxstamper.replace.PlaceholderReplacer;
import org.wickedsource.docxstamper.replace.typeresolver.FallbackResolver;

public class ProxyMethodHandlerTest {

	private PlaceholderReplacer placeholderReplacer = new PlaceholderReplacer(new TypeResolverRegistry(new FallbackResolver()));

	@Test
	public void proxyDelegatesToRegisteredCommentProcessors() throws Exception {

		CommentProcessorRegistry processorRegistry = new CommentProcessorRegistry(placeholderReplacer);
		processorRegistry.registerCommentProcessor(ITestInterface.class, new TestImpl());

		NameContext contextRoot = new NameContext();
		contextRoot.setName("Tom");
		NameContext context = new ProxyBuilder<NameContext>()
				.withRoot(contextRoot)
				.withInterface(ITestInterface.class, new TestImpl())
				.build();

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