package io.reflectoring.docxstamper.el;

import io.reflectoring.docxstamper.el.ExpressionUtil;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class ExpressionUtilTest {

    @Test
    public void findsPlaceholders() throws Exception {
        String text = "lorem ipsum ${placeholder1} lorem ipsum ${placeholder2}";

        ExpressionUtil finder = new ExpressionUtil();
        List<String> placeholders = finder.findVariableExpressions(text);

        Assert.assertEquals(2, placeholders.size());
        Assert.assertEquals("${placeholder1}", placeholders.get(0));
        Assert.assertEquals("${placeholder2}", placeholders.get(1));
    }

    @Test
    public void findsProcessorExpressions() throws Exception {
        String text = "lorem ipsum #{expression1} lorem ipsum #{expression2}";

        ExpressionUtil finder = new ExpressionUtil();
        List<String> placeholders = finder.findProcessorExpressions(text);

        Assert.assertEquals(2, placeholders.size());
        Assert.assertEquals("#{expression1}", placeholders.get(0));
        Assert.assertEquals("#{expression2}", placeholders.get(1));
    }

    @Test
    public void findsPlaceholdersWithError() throws Exception {
        String text = "lorem ipsum ${placeholder1} ${ lorem ipsum } ${placeholder2";

        ExpressionUtil finder = new ExpressionUtil();
        List<String> placeholders = finder.findVariableExpressions(text);

        Assert.assertEquals(2, placeholders.size());
        Assert.assertEquals("${placeholder1}", placeholders.get(0));
        Assert.assertEquals("${ lorem ipsum }", placeholders.get(1));
    }

    @Test
    public void returnsEmptyListOnEmptyText() {
        String text = "";
        ExpressionUtil finder = new ExpressionUtil();
        List<String> placeholders = finder.findVariableExpressions(text);
        Assert.assertTrue(placeholders.isEmpty());
    }

    @Test
    public void returnsEmptyListOnNullText() {
        ExpressionUtil finder = new ExpressionUtil();
        List<String> placeholders = finder.findVariableExpressions(null);
        Assert.assertTrue(placeholders.isEmpty());
    }

    @Test
    public void stripsExpressions() {
        ExpressionUtil finder = new ExpressionUtil();
        Assert.assertEquals("myExpression", finder.stripExpression("${myExpression}"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void stripsNullExpressionThrowsException() {
        ExpressionUtil finder = new ExpressionUtil();
        finder.stripExpression(null);
    }

}