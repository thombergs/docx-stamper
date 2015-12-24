package org.wickedsource.docxstamper;

import org.junit.Assert;
import org.junit.Test;
import org.wickedsource.docxstamper.expression.ExpressionUtil;

import java.util.List;

public class ExpressionUtilTest {

    @Test
    public void findsPlaceholders() throws Exception {
        String text = "lorem ipsum ${placeholder1} lorem ipsum ${placeholder2}";

        ExpressionUtil finder = new ExpressionUtil();
        List<String> placeholders = finder.findExpressions(text);

        Assert.assertEquals(2, placeholders.size());
        Assert.assertEquals("${placeholder1}", placeholders.get(0));
        Assert.assertEquals("${placeholder2}", placeholders.get(1));
    }

    @Test
    public void findsPlaceholdersWithError() throws Exception {
        String text = "lorem ipsum ${placeholder1} ${ lorem ipsum } ${placeholder2";

        ExpressionUtil finder = new ExpressionUtil();
        List<String> placeholders = finder.findExpressions(text);

        Assert.assertEquals(2, placeholders.size());
        Assert.assertEquals("${placeholder1}", placeholders.get(0));
        Assert.assertEquals("${ lorem ipsum }", placeholders.get(1));
    }

    @Test
    public void returnsEmptyListOnEmptyText() {
        String text = "";
        ExpressionUtil finder = new ExpressionUtil();
        List<String> placeholders = finder.findExpressions(text);
        Assert.assertTrue(placeholders.isEmpty());
    }

    @Test
    public void returnsEmptyListOnNullText() {
        ExpressionUtil finder = new ExpressionUtil();
        List<String> placeholders = finder.findExpressions(null);
        Assert.assertTrue(placeholders.isEmpty());
    }

    @Test
    public void stripsExpressions() {
        ExpressionUtil finder = new ExpressionUtil();
        Assert.assertEquals("myExpression", finder.stripExpression("${myExpression}"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void stripsNullExpressionThrowsException(){
        ExpressionUtil finder = new ExpressionUtil();
        finder.stripExpression(null);
    }

}